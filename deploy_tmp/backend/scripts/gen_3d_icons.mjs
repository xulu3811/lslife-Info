import fs from 'node:fs';
import path from 'node:path';
import sharp from 'sharp';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outputDir = path.resolve(__dirname, '../public/assets/icons');

if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

const icons = [
  {
    name: '3d_flat_secondhand.png',
    title: '个人闲置',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_idle" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FF8F00"/>
          <stop offset="100%" stop-color="#FF5252"/>
        </linearGradient>
        <linearGradient id="bag_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFF8E1"/>
          <stop offset="100%" stop-color="#FFD54F"/>
        </linearGradient>
        <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="12" stdDeviation="10" flood-color="#000000" flood-opacity="0.22"/>
        </filter>
        <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#000000" flood-opacity="0.15"/>
        </filter>
      </defs>
      <!-- Base Rounded Squircle -->
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_idle)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Minimalist Clay Shopping Bag -->
      <g filter="url(#shadow)">
        <path d="M 96 82 Q 96 48 128 48 Q 160 48 160 82" fill="none" stroke="#FFFFFF" stroke-width="14" stroke-linecap="round"/>
        <rect x="60" y="82" width="136" height="116" rx="28" ry="28" fill="url(#bag_grad)"/>
        <path d="M 60 102 Q 128 116 196 102 L 196 82 A 28 28 0 0 0 168 82 L 88 82 A 28 28 0 0 0 60 82 Z" fill="rgba(255,255,255,0.4)"/>
        <!-- Minimalist Verified Green Badge -->
        <circle cx="128" cy="142" r="26" fill="#10B981" filter="url(#glow)"/>
        <path d="M 114 142 L 124 152 L 144 132" fill="none" stroke="#FFFFFF" stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_housing.png',
    title: '房屋租售',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_house" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#29B6F6"/>
          <stop offset="100%" stop-color="#0288D1"/>
        </linearGradient>
        <linearGradient id="roof_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#40C4FF"/>
          <stop offset="100%" stop-color="#0056B3"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_house)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Minimalist Geometric House -->
      <g filter="url(#shadow)">
        <rect x="162" y="64" width="22" height="36" rx="6" fill="#90A4AE"/>
        <rect x="68" y="106" width="120" height="92" rx="18" ry="18" fill="#FFFFFF"/>
        <path d="M 48 116 L 128 52 L 208 116 Z" fill="url(#roof_grad)"/>
        <!-- Warm Cozy Glowing Window -->
        <rect x="94" y="132" width="68" height="46" rx="12" fill="#FFC107" filter="url(#glow)"/>
        <line x1="128" y1="132" x2="128" y2="178" stroke="#FFFFFF" stroke-width="4"/>
        <line x1="94" y1="155" x2="162" y2="155" stroke="#FFFFFF" stroke-width="4"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_cleaning.png',
    title: '家政保洁',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_clean" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#26A69A"/>
          <stop offset="100%" stop-color="#00695C"/>
        </linearGradient>
        <linearGradient id="bottle_grad" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#E0F2F1"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_clean)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Minimalist Cleaning Bottle + Sparkles -->
      <g filter="url(#shadow)">
        <rect x="108" y="78" width="40" height="18" fill="#00897B"/>
        <path d="M 104 68 L 156 68 A 10 10 0 0 1 166 78 L 166 96 L 94 96 L 94 78 A 10 10 0 0 1 104 68 Z" fill="#00ACC1"/>
        <rect x="166" y="76" width="18" height="12" rx="4" fill="#00E5FF"/>
        <rect x="90" y="96" width="76" height="102" rx="22" fill="url(#bottle_grad)"/>
        <!-- Minimalist Sparkles -->
        <path d="M 194 62 Q 194 82 214 82 Q 194 82 194 102 Q 194 82 174 82 Q 194 82 194 62 Z" fill="#FFF59D"/>
        <path d="M 64 136 Q 64 148 76 148 Q 64 148 64 160 Q 64 148 52 148 Q 64 148 64 136 Z" fill="#FFFFFF"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_repair.png',
    title: '水电维修',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_repair" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#9575CD"/>
          <stop offset="100%" stop-color="#512DA8"/>
        </linearGradient>
        <linearGradient id="wrench_grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#B0BEC5"/>
        </linearGradient>
        <linearGradient id="bolt_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFF59D"/>
          <stop offset="100%" stop-color="#FFB300"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_repair)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Wrench Crossed with Golden Lightning -->
      <g filter="url(#shadow)">
        <path d="M 64 176 L 140 100 A 34 34 0 1 1 180 140 L 104 216 A 22 22 0 1 1 64 176 Z" fill="url(#wrench_grad)"/>
        <circle cx="166" cy="114" r="14" fill="#512DA8"/>
        <path d="M 136 46 L 72 136 L 120 136 L 100 212 L 176 112 L 128 112 Z" fill="url(#bolt_grad)" stroke="#FFFFFF" stroke-width="4" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_produce.png',
    title: '水果蔬菜',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_veggie" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#81C784"/>
          <stop offset="100%" stop-color="#2E7D32"/>
        </linearGradient>
        <linearGradient id="veggie_grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#A5D6A7"/>
          <stop offset="100%" stop-color="#388E3C"/>
        </linearGradient>
        <linearGradient id="apple_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FF8A80"/>
          <stop offset="100%" stop-color="#D32F2F"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_veggie)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Minimalist Plump Apple & Jade Avocado/Leaf -->
      <g filter="url(#shadow)">
        <circle cx="94" cy="138" r="46" fill="url(#veggie_grad)"/>
        <circle cx="86" cy="132" r="18" fill="#E8F5E9" opacity="0.5"/>
        <circle cx="156" cy="144" r="52" fill="url(#apple_grad)"/>
        <path d="M 156 92 Q 162 80 170 78" fill="none" stroke="#5D4037" stroke-width="7" stroke-linecap="round"/>
        <path d="M 158 92 Q 132 60 176 50 Q 198 78 158 92 Z" fill="#66BB6A" filter="url(#glow)"/>
        <ellipse cx="138" cy="120" rx="14" ry="8" transform="rotate(-30 138 120)" fill="rgba(255,255,255,0.45)"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_jobs.png',
    title: '招聘求职',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_job" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#F06292"/>
          <stop offset="100%" stop-color="#C2185B"/>
        </linearGradient>
        <linearGradient id="case_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#90A4AE"/>
          <stop offset="100%" stop-color="#37474F"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_job)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Modern Briefcase & Verified Check -->
      <g filter="url(#shadow)">
        <path d="M 96 90 L 96 68 Q 96 56 110 56 L 146 56 Q 160 56 160 68 L 160 90" fill="none" stroke="#FFD54F" stroke-width="12" stroke-linecap="round"/>
        <rect x="48" y="90" width="160" height="110" rx="22" ry="22" fill="url(#case_grad)"/>
        <rect x="116" y="90" width="24" height="46" rx="6" fill="#263238"/>
        <circle cx="128" cy="124" r="9" fill="#FFCA28"/>
        <!-- Verified Cyan Badge -->
        <circle cx="178" cy="172" r="26" fill="#00E5FF" filter="url(#glow)"/>
        <path d="M 164 172 L 174 182 L 192 162" fill="none" stroke="#FFFFFF" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_car_rental.png',
    title: '租车服务',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_car" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFCA28"/>
          <stop offset="100%" stop-color="#E65100"/>
        </linearGradient>
        <linearGradient id="car_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#90CAF9"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_car)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Streamlined SUV/Car -->
      <g filter="url(#shadow)">
        <path d="M 48 144 L 72 96 Q 80 82 96 82 L 170 82 Q 186 82 194 96 L 214 144 Q 222 144 222 156 L 222 176 Q 222 184 214 184 L 48 184 Q 40 184 40 176 L 40 156 Q 40 144 48 144 Z" fill="url(#car_grad)"/>
        <!-- Tinted Cyber Windows -->
        <path d="M 78 96 L 126 96 L 126 136 L 58 136 Z" fill="#0288D1"/>
        <path d="M 134 96 L 182 96 L 202 136 L 134 136 Z" fill="#0277BD"/>
        <!-- Wheels -->
        <circle cx="82" cy="184" r="22" fill="#263238"/>
        <circle cx="82" cy="184" r="10" fill="#B0BEC5"/>
        <circle cx="180" cy="184" r="22" fill="#263238"/>
        <circle cx="180" cy="184" r="10" fill="#B0BEC5"/>
        <!-- Headlight Glow -->
        <rect x="208" y="150" width="14" height="8" rx="4" fill="#FFEB3B"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_parttime.png',
    title: '兼职零工',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg_parttime" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#7986CB"/>
          <stop offset="100%" stop-color="#303F9F"/>
        </linearGradient>
        <linearGradient id="timer_grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFE082"/>
          <stop offset="100%" stop-color="#FF8F00"/>
        </linearGradient>
        <linearGradient id="coin_grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFF59D"/>
          <stop offset="100%" stop-color="#FBC02D"/>
        </linearGradient>
      </defs>
      <rect x="16" y="22" width="224" height="224" rx="56" ry="56" fill="rgba(0,0,0,0.12)"/>
      <rect x="16" y="16" width="224" height="224" rx="56" ry="56" fill="url(#bg_parttime)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.28)"/>
      
      <!-- 3D Minimalist Timer & Glowing Gold Coin -->
      <g filter="url(#shadow)">
        <rect x="102" y="46" width="24" height="16" rx="6" fill="#FF8F00"/>
        <circle cx="114" cy="122" r="62" fill="url(#timer_grad)"/>
        <circle cx="114" cy="122" r="48" fill="#FFFFFF"/>
        <line x1="114" y1="122" x2="114" y2="86" stroke="#303F9F" stroke-width="7" stroke-linecap="round"/>
        <line x1="114" y1="122" x2="138" y2="122" stroke="#FF8F00" stroke-width="7" stroke-linecap="round"/>
        <!-- Gold Coin ¥ Reward -->
        <circle cx="172" cy="166" r="34" fill="url(#coin_grad)" filter="url(#glow)"/>
        <circle cx="172" cy="166" r="26" fill="none" stroke="#FFF9C4" stroke-width="2"/>
        <text x="172" y="178" font-size="36" font-weight="900" fill="#303F9F" text-anchor="middle" font-family="sans-serif">¥</text>
      </g>
    </svg>`
  }
];

async function generate() {
  console.log('Generating 3D flat PNG icons...');
  for (const item of icons) {
    const filePath = path.join(outputDir, item.name);
    const buffer = Buffer.from(item.svg);
    await sharp(buffer).png().toFile(filePath);
    console.log(`Generated: ${item.name} (${item.title}) -> ${filePath}`);
  }
  console.log('All 8 icons generated successfully.');
}

generate().catch(err => {
  console.error(err);
  process.exit(1);
});
