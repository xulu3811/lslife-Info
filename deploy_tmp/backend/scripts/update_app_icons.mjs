import sharp from 'sharp';
import fs from 'fs';
import path from 'path';

const sourceImg = 'C:/Users/xl246/.gemini/antigravity-ide/brain/2f4b64fc-19b5-4f3d-b4a5-7fa73692c9ac/media__1785136222175.png';

async function updateIcons() {
  if (!fs.existsSync(sourceImg)) {
    console.error('Source image not found:', sourceImg);
    process.exit(1);
  }

  const metadata = await sharp(sourceImg).metadata();
  console.log(`Source image metadata: ${metadata.width}x${metadata.height}, format: ${metadata.format}`);

  // Android Mipmap Targets
  const androidTargets = [
    { dir: 'd:/LsLife/android/app/src/main/res/mipmap-mdpi', size: 48 },
    { dir: 'd:/LsLife/android/app/src/main/res/mipmap-hdpi', size: 72 },
    { dir: 'd:/LsLife/android/app/src/main/res/mipmap-xhdpi', size: 96 },
    { dir: 'd:/LsLife/android/app/src/main/res/mipmap-xxhdpi', size: 144 },
    { dir: 'd:/LsLife/android/app/src/main/res/mipmap-xxxhdpi', size: 192 },
  ];

  for (const t of androidTargets) {
    if (!fs.existsSync(t.dir)) fs.mkdirSync(t.dir, { recursive: true });
    
    // Standard icon
    const launcherPath = path.join(t.dir, 'ic_launcher.png');
    await sharp(sourceImg)
      .resize(t.size, t.size, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
      .png({ quality: 100 })
      .toFile(launcherPath);

    // Round icon (create a circular mask)
    const roundPath = path.join(t.dir, 'ic_launcher_round.png');
    const circleSvg = `<svg><circle cx="${t.size/2}" cy="${t.size/2}" r="${t.size/2}" /></svg>`;
    await sharp(sourceImg)
      .resize(t.size, t.size, { fit: 'cover' })
      .composite([{
        input: Buffer.from(circleSvg),
        blend: 'dest-in'
      }])
      .png({ quality: 100 })
      .toFile(roundPath);
      
    console.log(`Updated Android icons in ${t.dir} (${t.size}x${t.size})`);
  }

  // Admin Web Targets
  const adminPublicDir = 'd:/LsLife/admin-web/public';
  const adminAssetsDir = 'd:/LsLife/admin-web/src/assets';
  if (!fs.existsSync(adminPublicDir)) fs.mkdirSync(adminPublicDir, { recursive: true });
  if (!fs.existsSync(adminAssetsDir)) fs.mkdirSync(adminAssetsDir, { recursive: true });

  await sharp(sourceImg)
    .resize(256, 256, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(adminPublicDir, 'favicon.png'));

  await sharp(sourceImg)
    .resize(64, 64, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(adminPublicDir, 'favicon.ico'));
    
  await sharp(sourceImg)
    .resize(512, 512, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(adminAssetsDir, 'logo.png'));
  console.log('Updated Admin Web icons');

  // Backend Public Assets
  const backendPublicDir = 'd:/LsLife/backend/public';
  const backendAssetsDir = 'd:/LsLife/backend/public/assets';
  if (!fs.existsSync(backendPublicDir)) fs.mkdirSync(backendPublicDir, { recursive: true });
  if (!fs.existsSync(backendAssetsDir)) fs.mkdirSync(backendAssetsDir, { recursive: true });

  await sharp(sourceImg)
    .resize(512, 512, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(backendAssetsDir, 'logo.png'));

  await sharp(sourceImg)
    .resize(512, 512, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(backendAssetsDir, 'app_icon.png'));

  await sharp(sourceImg)
    .resize(256, 256, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(backendPublicDir, 'favicon.png'));

  await sharp(sourceImg)
    .resize(64, 64, { fit: 'contain', background: { r: 255, g: 255, b: 255, alpha: 0 } })
    .png({ quality: 100 })
    .toFile(path.join(backendPublicDir, 'favicon.ico'));
  console.log('Updated Backend public asset icons');

  console.log('SUCCESS: All App Icons and Logos have been generated and synchronized across Frontend and Backend!');
}

updateIcons().catch(err => {
  console.error(err);
  process.exit(1);
});
