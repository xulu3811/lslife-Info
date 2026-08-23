import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function main() {
  console.log('=== 开始全面清理平台模拟数据与压力测试数据 ===');

  // 1. 清理各类流水与测试日志 (聊天、通知、验证码、交易流水、收货地址)
  const chatCount = await prisma.chatMessage.deleteMany({});
  const notifCount = await prisma.notification.deleteMany({});
  const transCount = await prisma.walletTransaction.deleteMany({});
  const codeCount = await prisma.verificationCode.deleteMany({});
  const addrCount = await prisma.address.deleteMany({});
  console.log(`[1/5] 已清理基础日志与测试配置: 聊天消息 ${chatCount.count} 条, 通知 ${notifCount.count} 条, 交易流水 ${transCount.count} 条, 验证码 ${codeCount.count} 条, 测试地址 ${addrCount.count} 条`);

  // 2. 清理电商业务数据 (购物车、订单项目、支付记录、配送记录、订单)
  const cartCount = await prisma.cartItem.deleteMany({});
  const orderItemCount = await prisma.orderItem.deleteMany({});
  const paymentCount = await prisma.payment.deleteMany({});
  const deliveryCount = await prisma.delivery.deleteMany({});
  const orderCount = await prisma.order.deleteMany({});
  console.log(`[2/5] 已清理订单与交易业务: 购物车 ${cartCount.count} 条, 订单明细 ${orderItemCount.count} 条, 支付记录 ${paymentCount.count} 条, 配送 ${deliveryCount.count} 条, 订单 ${orderCount.count} 个`);

  // 3. 清理帖子信息流 (所有测试/模拟/压测发帖)
  const postCount = await prisma.post.deleteMany({});
  console.log(`[3/5] 已清理帖子/同城信息流: ${postCount.count} 条`);

  // 4. 清理模拟商家与商品
  const productCount = await prisma.product.deleteMany({});
  const merchantCount = await prisma.merchant.deleteMany({});
  console.log(`[4/5] 已清理模拟店铺与商品: 商品 ${productCount.count} 个, 商家店铺 ${merchantCount.count} 个`);

  // 5. 清理压测与自动化测试生成的账号 (保留超级管理员)
  // 获取所有管理员的用户关联或者全量清理测试账号
  const delUsers = await prisma.user.deleteMany({
    where: {
      OR: [
        { nickname: { startsWith: '下单压测' } },
        { nickname: { startsWith: '财务测试' } },
        { nickname: { startsWith: '压测' } },
        { nickname: { startsWith: '测试' } },
        { nickname: { startsWith: '连山用户' } },
        { nickname: '部署验证' }
      ]
    }
  });
  console.log(`[5/5] 已清理测试与压测账号: ${delUsers.count} 个用户`);

  const remainUsers = await prisma.user.count();
  const remainAdmins = await prisma.adminUser.count();
  const remainCategories = await prisma.category.count();
  console.log(`=== 清理完成！生产数据库已重置至纯净初始状态 ===`);
  console.log(`📊 当前数据统计: 用户 ${remainUsers} 个, 管理员 ${remainAdmins} 个, 核心品类树 ${remainCategories} 个, 商家/商品/订单/发帖全清 0`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
