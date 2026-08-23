const { PrismaClient } = require('@prisma/client')

const prisma = new PrismaClient({
  datasources: {
    db: {
      url: "postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@115.191.6.95:5433/lslife?schema=public"
    }
  }
})

async function main() {
  const result = await prisma.category.updateMany({
    where: { name: '深度/开荒保洁' },
    data: { name: '深度保洁' }
  })
  console.log("Updated categories:", result.count)
  
  // Update any existing posts that might have the old category name in some fields if applicable
  // Wait, categories are linked by ID usually, so renaming the category is enough.
}

main()
  .catch(e => {
    console.error(e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
