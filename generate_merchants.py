import json

merchants_content = """import { Merchant } from '../types';

export const mockMerchants: Merchant[] = [
  {
    id: 'm1',
    name: '连山高山有机果蔬专营店',
    rating: 4.9,
    distance: 0.5,
    sales: 1200,
    avgPrice: 25,
    tags: ['有机蔬菜', '新鲜水果', '产地直供', '同城极速达'],
    deliveryFee: 0,
    deliveryTime: 20,
    logo: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&h=400&fit=crop&q=80',
    isFood: true,
    category: 'veggies',
    latitude: 24.4720,
    longitude: 112.0810,
    description: '连山本地高山有机果蔬直供，每日清晨采摘，新鲜直达。无农药残留，健康吃得出来。',
    address: '连山壮族瑶族自治县商业中心1楼',
    phone: '138-0000-1111',
    items: [
      {
        id: 'm1_1',
        name: '高山有机甜菜心 (500g)',
        price: 8.9,
        originalPrice: 12.9,
        desc: '清甜脆嫩，高山生态种植。',
        sales: 500,
        image: 'https://images.unsplash.com/photo-1566385101042-1a0104524c61?w=600&h=300&fit=crop&q=80',
        category: '蔬菜'
      },
      {
        id: 'm1_2',
        name: '新鲜特级红颜草莓 (1盒 约300g)',
        price: 25.8,
        originalPrice: 35.8,
        desc: '当天采摘，果大香甜。',
        sales: 300,
        image: 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&h=300&fit=crop&q=80',
        category: '水果'
      }
    ]
  },
  {
    id: 'm2',
    name: '连山好房中介服务',
    rating: 5.0,
    distance: 1.2,
    sales: 45,
    avgPrice: 1200,
    tags: ['真实房源', '拎包入住', '买卖租赁', '透明收费'],
    deliveryFee: 0,
    deliveryTime: 0,
    logo: 'https://images.unsplash.com/photo-1560518884-ce5882228a4b?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1560518884-ce5882228a4b?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'house',
    latitude: 24.4730,
    longitude: 112.0820,
    description: '专业房产中介，为您提供连山本地真实优质的租房、二手房买卖服务。',
    address: '连山壮族瑶族自治县吉水路8号',
    phone: '138-0000-2222',
    items: [
      {
        id: 'm2_1',
        name: '精装单间带阳台 (月租)',
        price: 800.0,
        originalPrice: 900.0,
        desc: '家电齐全，拎包入住，近商业中心。',
        sales: 12,
        image: 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600&h=300&fit=crop&q=80',
        category: '租房'
      },
      {
        id: 'm2_2',
        name: '三房两厅学区房 (长租)',
        price: 1500.0,
        desc: '采光极佳，近学校，适合家庭居住。',
        sales: 8,
        image: 'https://images.unsplash.com/photo-1502672260266-1c1e52416453?w=600&h=300&fit=crop&q=80',
        category: '租房'
      }
    ]
  },
  {
    id: 'm3',
    name: '连山王牌家政服务公司',
    rating: 4.8,
    distance: 2.0,
    sales: 890,
    avgPrice: 150,
    tags: ['全屋保洁', '专业团队', '钟点工', '售后保障'],
    deliveryFee: 0,
    deliveryTime: 60,
    logo: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'housekeeping',
    latitude: 24.4740,
    longitude: 112.0830,
    description: '本地老字号家政，阿姨经验丰富，工具齐全，提供高品质保洁服务。',
    address: '连山壮族瑶族自治县向阳街12号',
    phone: '138-0000-3333',
    items: [
      {
        id: 'm3_1',
        name: '日常全屋保洁 (3小时)',
        price: 135.0,
        originalPrice: 150.0,
        desc: '包括客厅、卧室、厨房、卫生间表面清洁，垃圾清运。',
        sales: 350,
        image: 'https://images.unsplash.com/photo-1527515637462-cff94eecc1ac?w=600&h=300&fit=crop&q=80',
        category: '保洁'
      },
      {
        id: 'm3_2',
        name: '深度开荒保洁 (100平米)',
        price: 450.0,
        desc: '新房入住前、二手房交易后深度清洁，含玻璃擦洗。',
        sales: 50,
        image: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600&h=300&fit=crop&q=80',
        category: '保洁'
      }
    ]
  },
  {
    id: 'm4',
    name: '张师傅极速水电维修',
    rating: 4.9,
    distance: 0.8,
    sales: 560,
    avgPrice: 80,
    tags: ['上门快', '经验丰富', '价格透明', '修不好不收费'],
    deliveryFee: 0,
    deliveryTime: 30,
    logo: 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'maintenance',
    latitude: 24.4750,
    longitude: 112.0840,
    description: '专业解决各类水电疑难杂症，管道疏通、电路检修。',
    address: '连山壮族瑶族自治县鹿鸣中路',
    phone: '138-0000-4444',
    items: [
      {
        id: 'm4_1',
        name: '马桶/下水道专业疏通',
        price: 80.0,
        desc: '不通不收费，专业设备快速解决堵塞。',
        sales: 200,
        image: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600&h=300&fit=crop&q=80',
        category: '维修'
      },
      {
        id: 'm4_2',
        name: '电路跳闸检测及维修',
        price: 60.0,
        desc: '专业仪器排查线路故障，更换空气开关等。',
        sales: 150,
        image: 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&h=300&fit=crop&q=80',
        category: '维修'
      }
    ]
  },
  {
    id: 'm5',
    name: '连山兄弟货运搬家',
    rating: 4.7,
    distance: 1.5,
    sales: 320,
    avgPrice: 200,
    tags: ['上门搬运', '货车出租', '专业打包', '安全准时'],
    deliveryFee: 0,
    deliveryTime: 40,
    logo: 'https://images.unsplash.com/photo-1600518464441-9154a4dea21b?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1600518464441-9154a4dea21b?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'moving',
    latitude: 24.4760,
    longitude: 112.0850,
    description: '专业同城搬家、货运服务，提供拆装打包、楼层搬运一条龙服务。',
    address: '连山壮族瑶族自治县沿江路',
    phone: '138-0000-5555',
    items: [
      {
        id: 'm5_1',
        name: '小面包人货混装同城送',
        price: 60.0,
        originalPrice: 80.0,
        desc: '适合少量物品搬运，同城快速送达。',
        sales: 180,
        image: 'https://images.unsplash.com/photo-1600518464441-9154a4dea21b?w=600&h=300&fit=crop&q=80',
        category: '搬家'
      },
      {
        id: 'm5_2',
        name: '4.2米厢货家庭搬家套餐',
        price: 260.0,
        desc: '包含2名搬运工，适合普通两居室家庭搬家。',
        sales: 90,
        image: 'https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?w=600&h=300&fit=crop&q=80',
        category: '搬家'
      }
    ]
  },
  {
    id: 'm6',
    name: '连山人才直聘中心',
    rating: 4.9,
    distance: 2.5,
    sales: 600,
    avgPrice: 0,
    tags: ['真实企业', '快速入职', '五险一金', '包食宿'],
    deliveryFee: 0,
    deliveryTime: 0,
    logo: 'https://images.unsplash.com/photo-1521791136064-7986c2920216?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1521791136064-7986c2920216?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'job',
    latitude: 24.4770,
    longitude: 112.0860,
    description: '连山本地权威招聘平台，提供海量真实工作岗位，帮您找到满意工作。',
    address: '连山壮族瑶族自治县广场南路',
    phone: '138-0000-6666',
    items: [
      {
        id: 'm6_1',
        name: '前台接待 (月薪4K+)',
        price: 0,
        originalPrice: 0,
        desc: '形象气质佳，具有亲和力，每周双休。',
        sales: 150,
        image: 'https://images.unsplash.com/photo-1521791136064-7986c2920216?w=600&h=300&fit=crop&q=80',
        category: '全职'
      },
      {
        id: 'm6_2',
        name: '餐厅服务员 (包吃住)',
        price: 0,
        desc: '勤劳肯干，男女不限，月薪3500加提成。',
        sales: 50,
        image: 'https://images.unsplash.com/photo-1556740738-b6a63e27c4df?w=600&h=300&fit=crop&q=80',
        category: '兼职'
      }
    ]
  },
  {
    id: 'm7',
    name: '连山闲置物品同城转让',
    rating: 4.6,
    distance: 1.0,
    sales: 120,
    avgPrice: 50,
    tags: ['九成新', '同城自提', '性价比高', '环保循环'],
    deliveryFee: 0,
    deliveryTime: 0,
    logo: 'https://images.unsplash.com/photo-1533105079780-92b9be482077?w=600&h=400&fit=crop&q=80',
    banner: 'https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800&h=400&fit=crop&q=80',
    isFood: false,
    category: 'secondhand',
    latitude: 24.4780,
    longitude: 112.0870,
    description: '个人闲置物品转让，涵盖数码、家居、母婴等各类二手好物。',
    address: '连山壮族瑶族自治县（全城）',
    phone: '138-0000-7777',
    items: [
      {
        id: 'm7_1',
        name: '九成新儿童自行车 (带辅助轮)',
        price: 80.0,
        originalPrice: 260.0,
        desc: '孩子长大了骑不了，车况极佳，自提优先。',
        sales: 1,
        image: 'https://images.unsplash.com/photo-1533105079780-92b9be482077?w=600&h=300&fit=crop&q=80',
        category: '二手'
      },
      {
        id: 'm7_2',
        name: '二手小米微波炉',
        price: 120.0,
        originalPrice: 350.0,
        desc: '搬家转让，功能完好无损，加热速度快。',
        sales: 1,
        image: 'https://images.unsplash.com/photo-1582735689369-4fe89db7114c?w=600&h=300&fit=crop&q=80',
        category: '二手'
      }
    ]
  }
];

export const USER_INITIAL_LOCATION = {
  lat: 24.4720,
  lng: 112.0810,
  name: '连山壮族瑶族自治县城市广场 (瑶香苑小区)'
};

export const merchantsData = mockMerchants;
"""

with open('src/data/merchants.ts', 'w') as f:
    f.write(merchants_content)
