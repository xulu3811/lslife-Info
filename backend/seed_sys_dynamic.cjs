const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
  console.log("Seeding sys_dynamic category...");
  
  const sysDynamic = await prisma.category.upsert({
    where: { id: 'sys_dynamic' },
    update: {
      name: '同城动态',
      isLeaf: true,
      isActive: true,
      attributeSchema: JSON.stringify([
        {
          id: 'text',
          label: '内容',
          type: 'text',
          required: true
        }
      ])
    },
    create: {
      id: 'sys_dynamic',
      name: '同城动态',
      isLeaf: true,
      isActive: true,
      attributeSchema: JSON.stringify([
        {
          id: 'text',
          label: '内容',
          type: 'text',
          required: true
        }
      ]),
      sortOrder: 999
    }
  });

  console.log("Upsert result:", sysDynamic);
}

main()
  .catch(e => {
    console.error("Error seeding sys_dynamic:", e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
