const fs = require('fs');
const path = 'D:/LsLife/backend/prisma/seed.ts';
let content = fs.readFileSync(path, 'utf8');

const veggiesOld = `  // 5. 同城生鲜
  {
    id: 'cat_veggies',
    name: '同城生鲜',
    icon: 'shopping-basket',
    iconUrl: '/assets/icons/3d_flat_fresh_food.png',
    sortOrder: 5,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_veg_meat_veg',
        name: '肉/蔬菜',
        icon: '🥬',
            iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veg_fruit',
        name: '水果',
        icon: '🍎',
            iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
    ],
  },`;

const veggiesNew = `  // 5. 同城生鲜
  {
    id: 'cat_veggies',
    name: '同城生鲜',
    icon: 'shopping-basket',
    iconUrl: '/assets/icons/3d_flat_fresh_food.png',
    sortOrder: 5,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_veggies_fruit',
        name: '新鲜水果',
        icon: '🍎',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_veg',
        name: '时令蔬菜',
        icon: '🥬',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_meat',
        name: '肉禽蛋品',
        icon: '🥩',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '种类', fieldType: 'SELECT', required: true, options: ['猪牛羊肉', '鸡鸭禽类', '蛋类', '其他'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_seafood',
        name: '海鲜水产',
        icon: '🦞',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'condition', label: '状态', fieldType: 'SELECT', required: true, options: ['活鲜', '冰鲜', '干货', '冷冻'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_frozen',
        name: '冷藏冻货',
        icon: '🧊',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '种类', fieldType: 'SELECT', required: true, options: ['速冻面点', '冻肉冻品', '火锅丸子', '冷饮冰品', '其他'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_oil',
        name: '粮油调味',
        icon: '🛢️',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_deli',
        name: '熟食卤味',
        icon: '🍗',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 7,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
    ],
  },`;

const diningOld = `  // 10. 餐饮娱乐
  {
    id: 'cat_dining',
    name: '餐饮娱乐',
    icon: 'restaurant',
    iconUrl: '/assets/icons/3d_flat_dining.png',
    sortOrder: 10,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'dining_food',
        name: '餐饮美食',
        icon: '🍲',

        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '餐饮类型', fieldType: 'TEXT', required: true, placeholder: '例如: 火锅/烧烤/农家乐' },
        ]),
      },
      {
        id: 'dining_new_open',
        name: '新店开张',
        icon: '🎊',

        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'shopType', label: '店铺类型', fieldType: 'TEXT', required: true, placeholder: '例如: 奶茶店/小吃店' },
        ]),
      },
      {
        id: 'dining_ktv_bar',
        name: 'KTV/酒吧',
        icon: '🎤',

        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '类型', fieldType: 'SELECT', required: true, options: ['KTV', '酒吧', '清吧', '夜总会'] },
        ]),
      }
    ],
  },`;

const diningNew = `  // 10. 餐饮娱乐
  {
    id: 'cat_dining',
    name: '餐饮娱乐',
    icon: 'restaurant',
    iconUrl: '/assets/icons/3d_flat_dining.png',
    sortOrder: 10,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_dining_food',
        name: '餐饮类',
        icon: '🍽️',
        iconUrl: '/assets/icons/3d_flat_dining.png',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'dining_fast_food',
            name: '快餐简餐',
            icon: '🍔',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 15元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 猪脚饭/盖浇饭' },
            ]),
          },
          {
            id: 'dining_drinks',
            name: '奶茶饮品',
            icon: '🧋',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 10元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 幽兰拿铁/手打柠檬茶' },
            ]),
          },
          {
            id: 'dining_bbq',
            name: '烧烤夜宵',
            icon: '🍢',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 50元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 烤生蚝/小龙虾' },
            ]),
          },
          {
            id: 'dining_hotpot',
            name: '火锅焖锅',
            icon: '🍲',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 80元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 重庆老火锅/潮汕牛肉' },
            ]),
          },
          {
            id: 'dining_restaurant',
            name: '炒菜正餐 / 地方菜',
            icon: '🍽️',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 5,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 60元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 湘菜/农家乐' },
            ]),
          },
          {
            id: 'dining_bakery',
            name: '甜点烘焙',
            icon: '🍰',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 6,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 30元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 生日蛋糕/脏脏包' },
            ]),
          }
        ]
      },
      {
        id: 'cat_dining_entertainment',
        name: '娱乐休闲类',
        icon: '🎮',
        iconUrl: '/assets/icons/3d_flat_dining.png',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'dining_ktv_bar',
            name: 'KTV / 酒吧',
            icon: '🎤',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 80元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 免费停车/豪华包间' },
            ]),
          },
          {
            id: 'dining_massage',
            name: '洗浴足疗',
            icon: '💆',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'duration', label: '服务时长', fieldType: 'TEXT', required: true, placeholder: '例如: 60分钟' },
              { key: 'therapist', label: '技师介绍', fieldType: 'TEXT', required: true, placeholder: '例如: 金牌技师/多年经验' },
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 120元' },
            ]),
          },
          {
            id: 'dining_billiards_cafe',
            name: '台球网咖',
            icon: '🎱',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 30元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 星牌台球/RTX4060电脑' },
            ]),
          },
          {
            id: 'dining_mahjong_tea',
            name: '棋牌茶室',
            icon: '🀄',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 50元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 机麻包厢/免费茶水' },
            ]),
          }
        ]
      }
    ],
  },`;

// To avoid exact string matching failure due to CRLF vs LF, we can normalize line endings.
content = content.replace(/\\r\\n/g, '\\n');
let modified = content;

// Since we have the exact text from the file (by reading it and viewing it), we can do a smart replace.
// Just to be safe, we replace the block between id: 'cat_veggies' and the next category id: 'cat_job'
const veggiesRegex = /  \/\/ 5\. 同城生鲜[\s\S]*?  \/\/ 6\. 招聘求职/;
modified = modified.replace(veggiesRegex, veggiesNew + '\\n  // 6. 招聘求职');

const diningRegex = /  \/\/ 10\. 餐饮娱乐[\s\S]*?\];/;
modified = modified.replace(diningRegex, diningNew + '\\n];');

if (content === modified) {
  console.log("No changes made. Regex failed.");
} else {
  fs.writeFileSync(path, modified);
  console.log("Success");
}
