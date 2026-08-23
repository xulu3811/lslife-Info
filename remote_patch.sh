#!/bin/bash
cd ~/lslife-backend
sed -i 's/provider = "sqlite"/provider = "postgresql"/g' prisma/schema.prisma
sed -i 's/127.0.0.1:5432/127.0.0.1:5433/g' .env
export PATH="$HOME/.local/nodejs/bin:$PATH"
npx prisma generate
npx prisma db push --accept-data-loss
npm run build
pm2 restart all
