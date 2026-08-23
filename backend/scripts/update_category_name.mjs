import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function main() {
  console.log('Starting global category rename in database (房租租售 -> 房屋租售)...');
  const c = await prisma.category.updateMany({
    where: { name: '房租租售' },
    data: { name: '房屋租售' }
  });
  console.log('Updated Category rows:', c.count);
  
  const p = await prisma.post.updateMany({
    where: { category: '房租租售' },
    data: { category: '房屋租售' }
  });
  console.log('Updated Post rows:', p.count);
  console.log('Database global rename completed successfully!');
}

main().catch(e => {
  console.error('Error updating database:', e);
  process.exit(1);
}).finally(() => prisma.$disconnect());
