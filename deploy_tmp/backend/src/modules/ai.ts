import { Router } from 'express';
import { z } from 'zod';
import { ok } from '../lib/http.js';
import { prisma } from '../lib/prisma.js';
import { asyncHandler } from '../middleware/error.js';
import { optionalAuth } from '../middleware/auth.js';
import { getAiProvider } from '../services/ai.js';

const router = Router();

/** AI 同城助手推荐 (国内合规大模型) */
router.post(
  '/recommend',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { prompt } = z.object({ prompt: z.string().min(1).max(500) }).parse(req.body);
    const result = await getAiProvider().recommend(prompt);
    return ok(res, result);
  }),
);

/** AI 闲鱼风商品文案生成与动态属性提取 */
router.post(
  '/generate-description',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { title, categoryId, draft, schema } = z
      .object({
        title: z.string().nullable().optional(),
        categoryId: z.string().nullable().optional(),
        draft: z.string().nullable().optional(),
        schema: z.array(z.any()).optional().default([]),
      })
      .parse(req.body);

    let targetSchema = schema;
    let categoryName = '同城发布';
    if (categoryId) {
      const cat = await prisma.category.findUnique({ where: { id: categoryId } });
      if (cat) {
        categoryName = cat.name;
        if ((!targetSchema || targetSchema.length === 0) && cat.attributeSchema) {
          try {
            targetSchema = JSON.parse(cat.attributeSchema);
          } catch {}
        }
      }
    }

    const hint = title || categoryName;
    const existing = (draft || '').trim();

    const schemaKeys = targetSchema.map((f: any) => f.key);
    const schemaDescription =
      targetSchema.length > 0
        ? `【当前分类动态属性 Schema 规格】：\n${JSON.stringify(targetSchema, null, 2)}\n【极其严格要求】：在输出 JSON 的 attributes 对象中，必须【仅使用】上述 Schema 中已定义的 Key（即: ${schemaKeys.join(
            ', ',
          )}）。严禁虚构、幻觉出未在 Schema 中定义的 Key！`
        : '无特定属性格式，尽力提取必要品牌和参数。';

    const prompt = `你是连山同城智能发布文案与实体提取助手。
用户准备在【${categoryName}】分类下发布标题/主题为“${hint}”的信息。
${
  existing
    ? `用户目前填写的草稿内容如下：\n【${existing}】\n请在【保留用户原文全部关键事实】的基础上进行优化润色。不要删掉用户已写信息，不要编造用户未提及的事实。`
    : '请写一段吸引人的转手/服务发布文案，字数控制在100字左右，包含转手原因或服务亮点。'
}

${schemaDescription}

你需要严格输出以下 JSON 格式：
{
  "title": "润色后的标题(30字以内)",
  "description": "润色后的详细描述正文",
  "attributes": {
    "key": "从草稿提取出的对应属性值"
  }
}`;

    const result = await getAiProvider().generateText(prompt);
    try {
      const parsed = JSON.parse(result);
      return ok(res, {
        title: parsed.title || hint,
        description: parsed.description || result,
        attributes: parsed.attributes || {},
      });
    } catch (e) {
      return ok(res, { title: hint, description: result, attributes: {} });
    }
  }),
);

export default router;
