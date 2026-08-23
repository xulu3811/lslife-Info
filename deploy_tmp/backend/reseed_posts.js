import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

const defaultUser = {
  id: "cms43gef7000010c43aw2ji9r", // Ensure this ID exists or use a generic one
};

async function main() {
  console.log('Clearing all existing posts...');
  await prisma.post.deleteMany({});
  
  // ensure the user exists
  const existingUser = await prisma.user.findFirst();
  if (!existingUser) {
    console.log("No user found, cannot seed posts!");
    process.exit(1);
  }

  const userId = existingUser.id;

  const mockPosts = [
    {
      publisherType: "INDIVIDUAL",
      listingType: "GOODS",
      postType: "CLASSIFIED",
      category: "cat_4_fresh_meat",
      title: "连山白切鸡 当日现做 新鲜卫生",
      description: "地道连山白切鸡，每日清晨现杀，肉质紧实，皮黄脆爽。35元/斤，包送货上门，欢迎品尝！",
      price: 35,
      images: JSON.stringify(["https://images.unsplash.com/photo-1598514982205-f36b96d1e8d4?w=800&q=80"]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ saleType: "零售", delivery: "同城配送" }),
      status: "published",
      reviewNote: "自动审核通过",
    },
    {
      publisherType: "INDIVIDUAL",
      listingType: "SERVICE",
      postType: "CLASSIFIED",
      category: "cat_10_edu_subject",
      title: "暑期英语特训 30天强化班",
      description: "专业英语四八级名师指导，针对中小学生暑期英语薄弱环节进行强化训练，快速提升成绩，680元一期。",
      price: 680,
      images: JSON.stringify(["https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800&q=80"]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ classType: "大班", time: "暑期" }),
      status: "published",
      reviewNote: "自动审核通过",
    },
    {
      publisherType: "INDIVIDUAL",
      listingType: "SERVICE",
      postType: "CLASSIFIED",
      category: "cat_7_ride_car",
      title: "汽车出租 连山本地租车 支持现场验车",
      description: "多款车型可选，轿车、SUV均有。价格透明，支持长租短租，车况极佳，支持咨询现场验车。",
      price: 150,
      images: JSON.stringify(["https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&q=80"]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ carType: "轿车", rentType: "日租" }),
      status: "published",
      reviewNote: "自动审核通过",
    },
    {
      publisherType: "INDIVIDUAL",
      listingType: "GOODS",
      postType: "CLASSIFIED",
      category: "cat_4_fresh_meat",
      title: "高山新鲜散养黄牛肉 38/斤 送货上门",
      description: "连山本地高山散养黄牛，肉质鲜美，绝不注水。38元一斤，同城免费配送上门。",
      price: 38,
      images: JSON.stringify(["https://images.unsplash.com/photo-1603048297172-c92544798d5e?w=800&q=80"]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ type: "猪牛羊肉", delivery: "同城配送" }),
      status: "published",
      reviewNote: "自动审核通过",
    },
    {
      publisherType: "INDIVIDUAL",
      listingType: "GOODS",
      postType: "CLASSIFIED",
      category: "cat_5_rent_share",
      title: "金御华府 120平精装三房急租",
      description: "金御华府优质房源，满五唯一，南北通透，精装修拎包入住，周边配套设施齐全，价格面议。",
      price: 1500,
      images: JSON.stringify([
        "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800&q=80",
        "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&q=80"
      ]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ layout: "3室2厅", area: "120" }),
      status: "published",
      reviewNote: "自动审核通过",
    },
    {
      publisherType: "INDIVIDUAL",
      listingType: "GOODS",
      postType: "CLASSIFIED",
      category: "cat_1_idle_3c",
      title: "自用99新 iPhone 15 Pro Max 256G",
      description: "自用手机，平时极其爱护，电池健康100%，无任何暗病磕碰。因换机出，支持同城面交。",
      price: 6500,
      images: JSON.stringify(["https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&q=80"]),
      locationName: "连山壮族瑶族自治县",
      attributes: JSON.stringify({ brand: "Apple", model: "iPhone 15 Pro Max", condition: "99新" }),
      status: "published",
      reviewNote: "自动审核通过",
    }
  ];

  console.log('Seeding new posts with valid Unsplash images...');
  for (const p of mockPosts) {
    await prisma.post.create({
      data: {
        ...p,
        userId: userId
      }
    });
  }

  console.log('Successfully seeded posts!');
  await prisma.$disconnect();
}

main().catch(e => {
  console.error(e);
  process.exit(1);
});
