import 'dotenv/config';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

const categoryTreeSeed = [
  {
    id: 'cat_1_idle',
    name: '个人闲置',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_idle',
    sortOrder: 1,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '数码3C', id: 'cat_1_idle_3c' },
      { name: '服饰箱包', id: 'cat_1_idle_clothing' },
      { name: '家电/家具', id: 'cat_1_idle_appliance' },
      { name: '美妆个护', id: 'cat_1_idle_beauty' },
      { name: '母婴儿童', id: 'cat_1_idle_baby' },
      { name: '运动/代步', id: 'cat_1_idle_sports' },
      { name: '文娱用品', id: 'cat_1_idle_hobby' },
      { name: '其他闲置', id: 'cat_1_idle_other' },
    ]
  },
  {
    id: 'cat_2_service',
    name: '家政/护理',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_service',
    sortOrder: 2,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '日常保洁', id: 'cat_2_service_daily', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_daily_cleaning_v4' },
      { name: '深度保洁', id: 'cat_2_service_deep', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_deep_cleaning_v4' },
      { name: '家电清洗', id: 'cat_2_service_appliance', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_appliance_clean_v9' },
      { name: '保姆/钟点工', id: 'cat_2_service_nanny', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_nanny_hourly_v3' },
      { name: '月嫂/育儿', id: 'cat_2_service_maternity', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_maternity_childcare_v3' },
      { name: '陪护/看护', id: 'cat_2_service_care', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_caregiving_v3' },
    ]
  },
  {
    id: 'cat_3_repair',
    name: '便民维修',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_repair',
    sortOrder: 3,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '水电/管道', id: 'cat_3_repair_plumbing', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_plumbing_v5' },
      { name: '开锁/换锁', id: 'cat_3_repair_lock', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_locksmith' },
      { name: '家电维修', id: 'cat_3_repair_appliance', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_repair_v2' },
      { name: '房屋修缮', id: 'cat_3_repair_house', iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sub_renovation_v2' },
      { name: '数码/电脑维修', id: 'cat_3_repair_pc' },
    ]
  },
  {
    id: 'cat_4_fresh',
    name: '同城生鲜',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_fresh',
    sortOrder: 4,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '新鲜水果', id: 'cat_4_fresh_fruit' },
      { name: '时令蔬菜', id: 'cat_4_fresh_veg' },
      { name: '肉禽蛋品', id: 'cat_4_fresh_meat' },
      { name: '海鲜水产', id: 'cat_4_fresh_seafood' },
      { name: '冷冻速食', id: 'cat_4_fresh_frozen' },
      { name: '粮油调味', id: 'cat_4_fresh_grocery' },
      { name: '熟食卤味', id: 'cat_4_fresh_deli' },
    ]
  },
  {
    id: 'cat_5_rent',
    name: '房屋出租',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_rent',
    sortOrder: 5,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '整租/合租', id: 'cat_5_rent_share' },
      { name: '商铺/办公出租', id: 'cat_5_rent_office' },
      { name: '厂房/仓库出租', id: 'cat_5_rent_warehouse' },
      { name: '日租/短租', id: 'cat_5_rent_daily' },
      { name: '车位出租', id: 'cat_5_rent_parking' },
    ]
  },
  {
    id: 'cat_6_sale',
    name: '二手房产',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_sale',
    sortOrder: 6,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '二手房出售', id: 'cat_6_sale_secondhand' },
      { name: '商铺/写字楼出售', id: 'cat_6_sale_office' },
      { name: '厂房/土地转让', id: 'cat_6_sale_land' },
      { name: '车位出售', id: 'cat_6_sale_parking' },
      { name: '新房/楼盘推荐', id: 'cat_6_sale_new' },
    ]
  },
  {
    id: 'cat_7_carpool',
    name: '拼车/租车',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_carpool',
    sortOrder: 7,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '车找人', id: 'cat_7_carpool_car2person' },
      { name: '人找车', id: 'cat_7_carpool_person2car' },
      { name: '顺路捎货', id: 'cat_7_carpool_freight' },
      { name: '搬家/同城货运', id: 'cat_7_carpool_moving' },
      { name: '汽车租赁', id: 'cat_7_carpool_rentcar' },
      { name: '婚车/车队租赁', id: 'cat_7_carpool_wedding' },
      { name: '大巴/工程车出租', id: 'cat_7_carpool_bus' },
    ]
  },
  {
    id: 'cat_8_job',
    name: '招聘求职',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_job',
    sortOrder: 8,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '全职直招', id: 'cat_8_job_fulltime' },
      { name: '兼职/日结', id: 'cat_8_job_parttime' },
      { name: '餐饮/服务业', id: 'cat_8_job_service' },
      { name: '普工/技工', id: 'cat_8_job_worker' },
      { name: '销售/客服', id: 'cat_8_job_sales' },
      { name: '司机/仓储', id: 'cat_8_job_driver' },
      { name: '行政/财务', id: 'cat_8_job_admin' },
    ]
  },
  {
    id: 'cat_9_life',
    name: '吃喝玩乐',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_life',
    sortOrder: 9,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '餐饮美食', id: 'cat_9_life_food' },
      { name: '休闲娱乐', id: 'cat_9_life_fun' },
      { name: '丽人养生', id: 'cat_9_life_beauty' },
      { name: '婚庆摄影', id: 'cat_9_life_wedding' },
      { name: '农家乐/周边游', id: 'cat_9_life_tour' },
    ]
  },
  {
    id: 'cat_10_edu',
    name: '教育培训',
    iconUrl: 'android.resource://com.lianshan.lslife/drawable/ic_category_edu',
    sortOrder: 10,
    isLeaf: false,
    isActive: true,
    
    children: [
      { name: '学科辅导', id: 'cat_10_edu_subject' },
      { name: '艺术/体育', id: 'cat_10_edu_art' },
      { name: '职业/考证', id: 'cat_10_edu_cert' },
      { name: '驾校报名', id: 'cat_10_edu_drive' },
      { name: '少儿早教', id: 'cat_10_edu_baby' },
      { name: 'AI/科技编程', id: 'cat_10_edu_ai' },
    ]
  },
];

async function main() {
  console.log('Clearing existing categories...');
  // Delete all existing categories to migrate to the new 10-major-category system
  await prisma.category.deleteMany();

  console.log('Seeding new 10 major categories and their children...');

  for (const parent of categoryTreeSeed) {
    // 1. Create root category
    const createdParent = await prisma.category.create({
      data: {
        id: parent.id,
        name: parent.name,
        iconUrl: parent.iconUrl,
        sortOrder: parent.sortOrder,
        isLeaf: parent.isLeaf,
        isActive: parent.isActive,
        
        attributeSchema: '[]' // Schema decoupled to schema_engine.ts
      }
    });

    console.log(`Created parent category: ${createdParent.name} (${createdParent.id})`);

    // 2. Create subcategories
    let subOrder = 1;
    for (const child of parent.children) {
      const subNameEng = child.id.replace(parent.id + '_', '');
      await prisma.category.create({
        data: {
          id: child.id,
          name: child.name,
          iconUrl: (child as any).iconUrl || `android.resource://com.lianshan.lslife/drawable/ic_category_sub_${subNameEng}`,
          parentId: createdParent.id,
          sortOrder: subOrder++,
          isLeaf: true,
          isActive: true,
          
          attributeSchema: '[]' // Schema decoupled to schema_engine.ts
        }
      });
    }
  }

  console.log('Category seed completed successfully.');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
