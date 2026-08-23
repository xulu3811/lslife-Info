import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient({
  datasources: {
    db: {
      url: "postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@localhost:5433/lslife"
    }
  }
});

async function main() {
  const passwordHash = await bcrypt.hash('ls441825', 10);
  
  const users = [
    {
      phone: 'test1',
      nickname: '普通用户(Test1)',
      passwordHash,
      role: 'USER_PERSONAL',
      freeQuota: 3,
      paidQuota: 0
    },
    {
      phone: 'test2',
      nickname: '充值用户(Test2)',
      passwordHash,
      role: 'USER_PERSONAL',
      freeQuota: 3,
      paidQuota: 99
    },
    {
      phone: 'test-s1',
      nickname: '普通商家(Test-S1)',
      passwordHash,
      role: 'MERCHANT_VERIFIED',
      freeQuota: 3,
      paidQuota: 0
    },
    {
      phone: 'test-s2',
      nickname: '充值商家(Test-S2)',
      passwordHash,
      role: 'MERCHANT_VERIFIED',
      freeQuota: 3,
      paidQuota: 99
    }
  ];

  for (const u of users) {
    const user = await prisma.user.upsert({
      where: { phone: u.phone },
      update: {
        passwordHash: u.passwordHash,
        role: u.role,
        paidQuota: u.paidQuota,
        freeQuota: u.freeQuota
      },
      create: {
        phone: u.phone,
        nickname: u.nickname,
        passwordHash: u.passwordHash,
        role: u.role,
        freeQuota: u.freeQuota,
        paidQuota: u.paidQuota
      }
    });
    
    // 如果是商家，还需要创建 MerchantInfo
    if (u.role === 'MERCHANT_VERIFIED') {
      await prisma.merchantInfo.upsert({
        where: { userId: user.id },
        update: { verifyStatus: 'APPROVED' },
        create: {
          userId: user.id,
          shopName: u.nickname + '的店铺',
          contactPhone: u.phone,
          address: '测试街道1号',
          verifyStatus: 'APPROVED'
        }
      });
    }

    console.log(`User created/updated: ${u.phone} (${u.role})`);
  }
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
