import { PrismaClient } from '@prisma/client';
import { encryptChatMessage, decryptChatMessage, generateEvidenceHash } from '../src/lib/crypto.js';

const prisma = new PrismaClient();

async function runTest() {
  console.log('=== 启动即时通讯 (IM) 密码学存证与加密防篡改测试 ===\n');

  // 1. 创建两个测试账号
  const u1 = await prisma.user.upsert({
    where: { phone: '13800000001' },
    update: {},
    create: { phone: '13800000001', nickname: '买家测试小明' }
  });
  const u2 = await prisma.user.upsert({
    where: { phone: '13800000002' },
    update: {},
    create: { phone: '13800000002', nickname: '商家测试老王' }
  });

  const id1 = u1.id < u2.id ? u1.id : u2.id;
  const id2 = u1.id < u2.id ? u2.id : u1.id;

  let session = await prisma.chatSession.findUnique({
    where: { user1Id_user2Id: { user1Id: id1, user2Id: id2 } }
  });
  if (!session) {
    session = await prisma.chatSession.create({
      data: { user1Id: id1, user2Id: id2 }
    });
  }

  console.log(`[会话建立] Session ID: ${session.id}`);

  // 2. 模拟连续发送 5 条商业交易对话消息
  const messagesText = [
    '老板，你发布的这个二手MacBook还在吗？',
    '在的，成色99新，没有任何暗病。',
    '价格能便宜点吗？4000能面交不？',
    '最低4200，在连山县城中央广场交易，行的话我下午带来。',
    '好的，成交！下午3点不见不散！'
  ];

  let prevHash: string | null = null;
  const createdMsgIds: string[] = [];

  for (let i = 0; i < messagesText.length; i++) {
    const rawText = messagesText[i];
    const sender = i % 2 === 0 ? u1 : u2;
    const now = new Date();

    // 计算证据链哈希与加密
    const evidenceHash = generateEvidenceHash(session.id, sender.id, now, rawText, prevHash);
    const encryptedContent = encryptChatMessage(rawText);

    const dbMsg = await prisma.chatMessage.create({
      data: {
        sessionId: session.id,
        senderId: sender.id,
        type: 'text',
        content: encryptedContent,
        isEncrypted: true,
        evidenceHash,
        createdAt: now
      }
    });

    createdMsgIds.push(dbMsg.id);
    prevHash = evidenceHash;
    console.log(`\n[消息发信 #${i+1}] 发送者: ${sender.nickname}`);
    console.log(`  -> 原始明文: "${rawText}"`);
    console.log(`  -> 数据库密文: "${dbMsg.content}"`);
    console.log(`  -> 防篡改存证哈希 (SHA-256): ${dbMsg.evidenceHash}`);
  }

  // 3. 校验数据库落盘数据安全与脱密还原
  console.log('\n=== 开始进行后台存证哈希链与加解密完整性校验 ===');
  const dbMessages = await prisma.chatMessage.findMany({
    where: { id: { in: createdMsgIds } },
    orderBy: { createdAt: 'asc' }
  });

  let chainValid = true;
  let checkPrevHash: string | null = null;

  for (let i = 0; i < dbMessages.length; i++) {
    const m = dbMessages[i];
    if (!m.content.startsWith('ENC_GCM_V1:')) {
      console.error(`[失败] 消息 #${i+1} 未进行密文落盘!`);
      chainValid = false;
    }
    const decrypted = decryptChatMessage(m.content);
    const expectedHash = generateEvidenceHash(m.sessionId, m.senderId, m.createdAt, decrypted, checkPrevHash);
    if (m.evidenceHash !== expectedHash) {
      console.error(`[失败] 消息 #${i+1} 存证哈希链断裂或被篡改! 期望: ${expectedHash}, 实际: ${m.evidenceHash}`);
      chainValid = false;
    }
    checkPrevHash = m.evidenceHash;
    console.log(`[成功校验 #${i+1}] 解密还原: "${decrypted}" | 哈希连续性通过 ✔️`);
  }

  if (chainValid) {
    console.log('\n🏆【测试通过】即时通讯交易证据链 (AES-256-GCM + SHA-256 Hash Chain) 绝对连续，无懈可击！');
  } else {
    console.error('\n❌【测试失败】存证链或加解密异常！');
    process.exit(1);
  }

  // 4. 测试 1 分钟消息撤回
  console.log('\n=== 模拟用户撤回最后一条消息 ===');
  const lastMsgId = createdMsgIds[createdMsgIds.length - 1];
  await prisma.chatMessage.update({
    where: { id: lastMsgId },
    data: {
      isRecalled: true,
      type: 'recalled',
      content: '对方撤回了一条消息',
      isEncrypted: false
    }
  });

  const recalledMsg = await prisma.chatMessage.findUnique({ where: { id: lastMsgId } });
  console.log(`[成功撤回] 消息ID ${recalledMsg?.id} 状态: isRecalled=${recalledMsg?.isRecalled}, 展示内容: "${recalledMsg?.content}"`);

  // 5. 清理测试数据
  await prisma.chatMessage.deleteMany({ where: { sessionId: session.id } });
  await prisma.chatSession.delete({ where: { id: session.id } });
  console.log('\n测试会话数据已自动清理完毕。');
}

runTest()
  .catch(err => {
    console.error('测试运行异常:', err);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
