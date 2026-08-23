import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function main() {
  const commerceIds = ["cat_idle", "cat_veggies", "cat_service", "cat_maintenance", "cat_dining"];
  const infoIds = ["cat_house", "cat_job", "cat_part_time", "cat_car_rental", "cat_education"];

  // Helper to recursively update
  async function updateTree(parentId, mode) {
    await prisma.category.update({ where: { id: parentId }, data: { tradeMode: mode } }).catch(() => {});
    const children = await prisma.category.findMany({ where: { parentId } });
    for (const child of children) {
      await updateTree(child.id, mode);
    }
  }

  for (const id of commerceIds) {
    let mode = 'COMMERCE';
    if (id === 'cat_idle') mode = 'C2C_IDLE';
    else if (id === 'cat_veggies' || id === 'cat_dining') mode = 'O2O_STORE';
    else if (id === 'cat_service' || id === 'cat_maintenance') mode = 'SERVICE_ORDER';
    
    await updateTree(id, mode);
  }

  for (const id of infoIds) {
    await updateTree(id, 'INFO_PUBLISH');
  }

  // Also update any existing posts that might have wrong tradeMode
  for (const id of commerceIds) {
      const children = await prisma.category.findMany({ where: { OR: [{id: id}, {parentId: id}] } });
      const childIds = children.map(c => c.id);
      childIds.push(id);
      
      let mode = 'COMMERCE';
      if (id === 'cat_idle') mode = 'C2C_IDLE';
      else if (id === 'cat_veggies' || id === 'cat_dining') mode = 'O2O_STORE';
      else if (id === 'cat_service' || id === 'cat_maintenance') mode = 'SERVICE_ORDER';
      
      await prisma.post.updateMany({
          where: { categoryId: { in: childIds } },
          data: { tradeMode: mode }
      });
  }
  for (const id of infoIds) {
      const children = await prisma.category.findMany({ where: { OR: [{id: id}, {parentId: id}] } });
      const childIds = children.map(c => c.id);
      childIds.push(id);
      
      await prisma.post.updateMany({
          where: { categoryId: { in: childIds } },
          data: { tradeMode: 'INFO_PUBLISH' }
      });
  }

  console.log("Database tradeMode fixed successfully!");
}

main().finally(() => prisma.$disconnect());
