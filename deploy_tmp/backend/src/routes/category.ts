import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';

const router = Router();

interface CategoryNodeResponse {
  id: string;
  name: string;
  icon: string | null;
  iconUrl: string | null;
  parentId: string | null;
  sortOrder: number;
  isLeaf: boolean;
  isActive: boolean;
  isHot: boolean;
  attributeSchema: any[];
  children: CategoryNodeResponse[];
}

/** 获取全量有效分类树结构 (层级树 Parent -> Children) */
router.get(
  '/tree',
  asyncHandler(async (_req, res) => {
    // 兼容查询，若运行了新版迁移则过滤 isActive=true；兼容未包含字段的老数据
    const allCategories = await prisma.category.findMany({
      where: {
        OR: [
          { isActive: true },
          { isActive: { equals: undefined } }
        ]
      },
      orderBy: { sortOrder: 'asc' },
    });

    const buildTree = (parentId: string | null): CategoryNodeResponse[] => {
      return allCategories
        .filter((c) => c.parentId === parentId)
        .map((c) => {
          let schema: any[] = [];
          try {
            if (c.attributeSchema) {
              schema = typeof c.attributeSchema === 'string' ? JSON.parse(c.attributeSchema) : c.attributeSchema;
            }
          } catch {
            schema = [];
          }
          const iconUrlValue = c.iconUrl ?? null;
          const iconValue = c.icon ?? null;
          return {
            id: c.id,
            name: c.name,
            icon: iconValue,
            iconUrl: iconUrlValue,
            parentId: c.parentId,
            sortOrder: c.sortOrder,
            isLeaf: c.isLeaf,
            isActive: (c as any).isActive !== undefined ? (c as any).isActive : true,
            isHot: (c as any).isHot ?? false,
            attributeSchema: schema,
            children: buildTree(c.id),
          };
        });
    };

    const tree = buildTree(null);
    return ok(res, tree);
  }),
);

/** 获取热门推荐分类列表 (用于首页或发布快捷选区) */
router.get(
  '/hot',
  asyncHandler(async (_req, res) => {
    const hotCategories = await prisma.category.findMany({
      where: {
        isHot: true,
        OR: [
          { isActive: true },
          { isActive: { equals: undefined } }
        ]
      },
      orderBy: { sortOrder: 'asc' },
    });

    const result = hotCategories.map((c) => {
      let schema: any[] = [];
      try {
        if (c.attributeSchema) {
          schema = typeof c.attributeSchema === 'string' ? JSON.parse(c.attributeSchema) : c.attributeSchema;
        }
      } catch {
        schema = [];
      }
      return {
        id: c.id,
        name: c.name,
        icon: c.icon ?? null,
        iconUrl: c.iconUrl ?? null,
        parentId: c.parentId,
        sortOrder: c.sortOrder,
        isLeaf: c.isLeaf,
        isActive: (c as any).isActive !== undefined ? (c as any).isActive : true,
        isHot: (c as any).isHot ?? false,
        attributeSchema: schema,
      };
    });

    return ok(res, result);
  }),
);

/** 获取特定叶子分类的 Dynamic Attribute Schema */
router.get(
  '/:id/schema',
  asyncHandler(async (req, res) => {
    const category = await prisma.category.findUnique({
      where: { id: req.params.id },
    });
    if (!category) {
      throw new ApiError(404, '分类不存在');
    }

    let schema: any[] = [];
    try {
      if (category.attributeSchema) {
        schema = typeof category.attributeSchema === 'string' ? JSON.parse(category.attributeSchema) : category.attributeSchema;
      }
    } catch {
      schema = [];
    }

    return ok(res, {
      categoryId: category.id,
      name: category.name,
      isLeaf: category.isLeaf,
      attributeSchema: schema,
    });
  }),
);

export default router;
