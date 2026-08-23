import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient({
  datasources: {
    db: {
      url: "postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@localhost:5433/lslife"
    }
  }
});

async function main() {
  const phones = ['test1', 'test2', 'test-s1', 'test-s2'];
  for (const phone of phones) {
    try {
      await prisma.user.delete({
        where: { phone }
      });
      console.log(`✅ Deleted test account: ${phone}`);
    } catch (e) {
      if (e.code === 'P2025') {
        console.log(`⚠️ User not found (already deleted): ${phone}`);
      } else {
        console.error(`❌ Failed to delete ${phone}:`, e.message);
      }
    }
  }
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
