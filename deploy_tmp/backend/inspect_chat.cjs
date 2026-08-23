const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
  const sessions = await prisma.chatSession.findMany({
    include: {
      messages: true
    }
  });
  console.log("ChatSessions:", JSON.stringify(sessions, null, 2));
}

main().catch(console.error).finally(() => prisma.$disconnect());
