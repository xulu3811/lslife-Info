const fs = require('fs');
const path = require('path');

const seedPath = path.join(__dirname, '../prisma/seed.ts');
let content = fs.readFileSync(seedPath, 'utf-8');

const prefixMapping = {
    'cat_job_hospitality': '3d_flat_job_hospitality.png',
    'cat_job_blue_collar': '3d_flat_job_blue_collar.png',
    'cat_job_sales': '3d_flat_job_sales.png',
    'cat_job_admin': '3d_flat_job_admin.png',
    'cat_job_logistics': '3d_flat_job_logistics.png',
    'cat_job_other': '3d_flat_job_other.png',
    'cat_pt_temp': '3d_flat_pt_temp.png',
    'cat_pt_promo': '3d_flat_pt_promo.png',
    'cat_pt_hotel': '3d_flat_pt_hotel.png',
    'cat_pt_tutor': '3d_flat_pt_tutor.png',
    'cat_pt_errand': '3d_flat_pt_errand.png',
};

// Match ID and Name
const regex = /id:\s*'([^']+)',\s*\n\s*name:\s*'([^']+)',/g;
let match;
let matchCount = 0;

while ((match = regex.exec(content)) !== null) {
    const id = match[1];
    
    if (prefixMapping[id]) {
        // Check if the next characters already include iconUrl
        const nextPart = content.substring(match.index, match.index + 200);
        if (!nextPart.includes("iconUrl:")) {
            const searchString = match[0];
            const replacement = searchString + "\n        icon: 'folder',\n        iconUrl: '/assets/icons/" + prefixMapping[id] + "',";
            content = content.replace(searchString, replacement);
            matchCount++;
            regex.lastIndex = 0;
        }
    }
}

fs.writeFileSync(seedPath, content, 'utf-8');
console.log('Successfully injected', matchCount, 'iconUrls for job and pt into seed.ts');
