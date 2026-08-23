require('dotenv').config();
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
async function main() {
    const users = await prisma.user.findMany();
    console.log(users.map(u => ({ phone: u.phone, role: u.role, passwordHash: u.passwordHash != null })));
}
main().finally(() => prisma.$disconnect());
