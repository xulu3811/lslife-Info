import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rawDir = path.join(__dirname, '../public/assets/icons/raw');
const outDir = path.join(__dirname, '../public/assets/icons');

// 确保目录存在
if (!fs.existsSync(rawDir)) {
  fs.mkdirSync(rawDir, { recursive: true });
}
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// 目标图标文件名映射 (为了代码统一性，使用英文命名)
const iconMapping = {
  '图1-个人闲置.png': 'idle.webp',
  '图2-房屋租售.png': 'house.webp',
  '图3-家政保洁.png': 'cleaning.webp',
  '图4-水电维修.png': 'repair.webp',
  // 兼容用户可能上传的不同命名
  '拼车租车.png': 'carpool.webp',
  '房屋租售.png': 'house.webp',
  '家政保洁.png': 'cleaning.webp',
  '水电维修.png': 'repair.webp'
};

async function processIcons() {
  const files = fs.readdirSync(rawDir).filter(f => f.endsWith('.png') || f.endsWith('.jpg') || f.endsWith('.jpeg'));
  
  if (files.length === 0) {
    console.log('⚠️ 请先将需要处理的原始图片放入目录:', rawDir);
    return;
  }

  for (const file of files) {
    const rawPath = path.join(rawDir, file);
    // 根据映射或者直接以原名替换后缀
    const outName = iconMapping[file] || file.replace(/\.(png|jpg|jpeg)$/i, '.webp');
    const outPath = path.join(outDir, outName);

    console.log(`Processing ${file} -> ${outName}...`);
    try {
      const image = sharp(rawPath);
      const metadata = await image.metadata();

      // 假设图片上方是 1:1 的正方形圆角图标，下方是文字
      // 我们按照图片的宽度来截取顶部的正方形区域
      const cropSize = metadata.width; // 取宽度作为正方形的边长

      if (metadata.height < cropSize) {
         // 如果高度小于宽度，可能不是典型的“上面图标下面文字”比例，直接按中心裁剪
         await image
           .resize(256, 256, { fit: 'cover' })
           .webp({ quality: 85, effort: 6 }) // 无损转换高压缩比
           .toFile(outPath);
      } else {
         // 抠出顶部的正方形部分 (x:0, y:0, width: cropSize, height: cropSize)
         await image
           .extract({ left: 0, top: 0, width: cropSize, height: cropSize })
           .resize(256, 256, { fit: 'cover' })
           .webp({ quality: 85, effort: 6 })
           .toFile(outPath);
      }
      
      console.log(`✅ Successfully generated ${outName}`);
    } catch (err) {
      console.error(`❌ Failed to process ${file}:`, err);
    }
  }

  console.log('\n🎉 所有图标处理完成！您可以运行 node scripts/upload_icons.mjs 上传到服务器，然后运行 update_category_icons.ts 更新数据库。');
}

processIcons();
