const { getAiProvider } = require('./dist/services/ai.js');

async function main() {
  const provider = getAiProvider();
  console.log('Testing DeepSeek AI NLP Extraction...');
  const prompt = `你是连山同城智能发布文案与实体提取助手。
用户准备在【二手手机】分类下发布标题/主题为“闲置 iPhone 13”的信息。
用户目前填写的草稿内容如下：
【iPhone 13 128G 蓝色，99新，没有任何划痕，带原装充电器，因为换了新手机所以出了，电池健康98%。】
请在【保留用户原文全部关键事实】的基础上进行优化润色。不要删掉用户已写信息，不要编造用户未提及的事实。

【当前分类动态属性 Schema 规格】：
[
  { "key": "brand", "label": "品牌", "type": "string" },
  { "key": "condition", "label": "成色", "type": "string" },
  { "key": "storage", "label": "容量", "type": "string" }
]
【极其严格要求】：在输出 JSON 的 attributes 对象中，必须【仅使用】上述 Schema 中已定义的 Key（即: brand, condition, storage）。严禁虚构、幻觉出未在 Schema 中定义的 Key！

你需要严格输出以下 JSON 格式：
{
  "title": "润色后的标题(30字以内)",
  "description": "润色后的详细描述正文",
  "attributes": {
    "key": "从草稿提取出的对应属性值"
  }
}`;

  try {
    const res = await provider.generateText(prompt);
    console.log('DeepSeek AI response:', res);
  } catch(e) {
    console.error('Error:', e);
  }
}

main();
