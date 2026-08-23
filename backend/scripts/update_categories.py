import re

with open(r'd:\LsLife\backend\prisma\seed.ts', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Remove extra children from cat_veggies
pattern_veggies = re.compile(r"(        id: 'cat_veg_fruit',.*?          \{ key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' \},\s*\]\),\s*\},).*?(  // 6\. 招聘求职)", re.DOTALL)
content = pattern_veggies.sub(r"\1\n    ],\n  },\n\2", content)

# 2. Update cat_car_rental
# Change name to 拼车/租车
content = content.replace("id: 'cat_car_rental',\n    name: '租车服务',", "id: 'cat_car_rental',\n    name: '拼车/租车',")

# Replace the children of cat_car_rental.
car_rental_children_new = """    children: [
      {
        id: 'car_carpool_person_find_car',
        name: '人找车',
        icon: '🙋',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'departure', label: '出发地', fieldType: 'TEXT', required: true, placeholder: '例如: 连山县城' },
          { key: 'destination', label: '目的地', fieldType: 'TEXT', required: true, placeholder: '例如: 清远市区' },
          { key: 'time', label: '出发时间', fieldType: 'TEXT', required: true, placeholder: '例如: 明早8点' },
          { key: 'people', label: '乘车人数', fieldType: 'TEXT', required: true, placeholder: '例如: 2人' },
        ]),
      },
      {
        id: 'car_carpool_car_find_person',
        name: '车找人',
        icon: '🚘',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'departure', label: '出发地', fieldType: 'TEXT', required: true, placeholder: '例如: 连山' },
          { key: 'destination', label: '目的地', fieldType: 'TEXT', required: true, placeholder: '例如: 广州' },
          { key: 'time', label: '出发时间', fieldType: 'TEXT', required: true, placeholder: '例如: 今晚6点' },
          { key: 'seats', label: '空余座位', fieldType: 'TEXT', required: true, placeholder: '例如: 3座' },
        ]),
      },
      {
        id: 'car_carpool_goods',
        name: '捎带货',
        icon: '📦',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'route', label: '起止路线', fieldType: 'TEXT', required: true, placeholder: '例如: 连山-连州' },
          { key: 'goods', label: '货物描述', fieldType: 'TEXT', required: true, placeholder: '例如: 两个小纸箱' },
        ]),
      },
      {
        id: 'car_rental_suv',
        name: '轿车 / SUV 租赁',
        icon: '🚗',
        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车型', fieldType: 'TEXT', required: true, placeholder: '例如: 丰田卡罗拉' },
          { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['日租', '月租'] },
        ]),
      },
      {
        id: 'car_rental_wedding',
        name: '婚车租赁',
        icon: '🎀',
        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '婚车车型', fieldType: 'TEXT', required: true, placeholder: '例如: 奔驰S级 / 宝马7系' },
          { key: 'rentType', label: '服务包含', fieldType: 'SELECT', required: true, options: ['带司机', '自驾'] },
        ]),
      },
      {
        id: 'car_rental_bus',
        name: '大巴商务车租赁',
        icon: '🚌',
        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车型', fieldType: 'TEXT', required: true, placeholder: '例如: 别克GL8 / 考斯特' },
          { key: 'seats', label: '座位数', fieldType: 'TEXT', required: true, placeholder: '例如: 7座 / 20座' },
        ]),
      },
      {
        id: 'car_rental_truck',
        name: '货车/工程车租赁',
        icon: '🚚',
        sortOrder: 7,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车辆类型', fieldType: 'TEXT', required: true, placeholder: '例如: 4.2米箱货 / 挖掘机' },
        ]),
      }
    ],
  },
  // 8. 兼职零工"""

pattern_car = re.compile(r"    children: \[\s*\{\s*id: 'cat_car_suv',.*?(  // 8\. 兼职零工)", re.DOTALL)
content = pattern_car.sub(car_rental_children_new, content)

# 3. Add 教育培训 and 餐饮娱乐 at the end of categoryTreeSeed
new_cats = """
  // 9. 教育培训
  {
    id: 'cat_education',
    name: '教育培训',
    icon: 'school',
    iconUrl: '/assets/icons/3d_flat_education.png',
    sortOrder: 9,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'edu_k12',
        name: '中小学辅导',
        icon: '📚',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'subject', label: '科目', fieldType: 'TEXT', required: true, placeholder: '例如: 数学/英语' },
        ]),
      },
      {
        id: 'edu_art',
        name: '艺术',
        icon: '🎨',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'subject', label: '类型', fieldType: 'TEXT', required: true, placeholder: '例如: 美术/音乐' },
        ]),
      },
      {
        id: 'edu_driving',
        name: '驾校招考',
        icon: '🚗',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'licenseType', label: '驾照类型', fieldType: 'SELECT', required: true, options: ['C1', 'C2', 'B2', 'A1'] },
        ]),
      },
      {
        id: 'edu_vocational',
        name: '职业技能',
        icon: '🔧',
        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'skill', label: '技能方向', fieldType: 'TEXT', required: true, placeholder: '例如: 厨师/电工/美容' },
        ]),
      },
      {
        id: 'edu_english',
        name: '英语',
        icon: '🔤',
        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'level', label: '适合水平', fieldType: 'TEXT', required: true, placeholder: '例如: 四六级/雅思' },
        ]),
      },
      {
        id: 'edu_ai_science',
        name: 'AI编程/科学',
        icon: '💻',
        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'course', label: '课程内容', fieldType: 'TEXT', required: true, placeholder: '例如: 少儿编程/机器人' },
        ]),
      }
    ],
  },
  // 10. 餐饮娱乐
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
  },
];"""

content = content.replace("    ],\n  },\n];", "    ],\n  },\n" + new_cats)

with open(r'd:\LsLife\backend\prisma\seed.ts', 'w', encoding='utf-8') as f:
    f.write(content)
