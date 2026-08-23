import Tesseract from 'tesseract.js';
import fs from 'fs';
import path from 'path';

async function extractIDCardInfo(imageUrlOrPath) {
  try {
    const { data: { text } } = await Tesseract.recognize(
      imageUrlOrPath,
      'chi_sim',
      { logger: m => console.log(m) }
    );
    
    console.log("Raw OCR Text:\n", text);
    
    // Extract Name
    // Usually "姓名 许路" or "名 许路"
    let realName = '';
    const nameMatch = text.match(/姓\s*名[\s:]*([^\n\d]+)/);
    if (nameMatch) {
      realName = nameMatch[1].replace(/[\s\r]/g, '');
    }

    // Extract ID
    // Usually "公民身份号码 370831198904120717"
    let idCard = '';
    const idMatch = text.match(/(\d{17}[\dXx])/);
    if (idMatch) {
      idCard = idMatch[1];
    }

    return { realName, idCard };
  } catch (error) {
    console.error("OCR Error:", error);
    return null;
  }
}

async function main() {
  const imagePath = "C:/Users/xl246/.gemini/antigravity-ide/brain/4899befb-7b8a-4c81-8383-065c4089938a/.user_uploaded/media_1787377944177.jpg"; // Absolute path to the user's uploaded image
  const res = await extractIDCardInfo(imagePath);
  console.log("Extracted:", res);
}

main();
