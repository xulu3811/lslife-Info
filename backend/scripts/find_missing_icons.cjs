const fs = require('fs');
const path = require('path');

const seedPath = path.join(__dirname, '../prisma/seed.ts');
const content = fs.readFileSync(seedPath, 'utf-8');

const regex = /id:\s*'([^']+)',\s*\n\s*name:\s*'([^']+)',\s*\n\s*icon:\s*'([^']+)',(?!\s*\n\s*iconUrl:)/g;
let match;
while ((match = regex.exec(content)) !== null) {
    if (!match[1].startsWith('cat_car_rental') && !match[1].startsWith('cat_house') && match[1] !== 'cat_idle' && !match[1].startsWith('cat_service') && !match[1].startsWith('cat_main')) {
        // filter out top level categories if any
        if (match[2] !== '个人闲置' && match[2] !== '房屋租售' && match[2] !== '家政保洁' && match[2] !== '水电维修' && match[2] !== '招聘求职' && match[2] !== '兼职零工' && match[2] !== '拼车/租车' && match[2] !== '教育培训' && match[2] !== '餐饮娱乐' && match[2] !== '同城生鲜') {
            console.log(`'${match[1]}': '', // ${match[2]}`);
        }
    }
}
