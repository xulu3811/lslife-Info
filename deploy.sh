#!/bin/bash
export PATH="$HOME/.local/nodejs/bin:$PATH"
cd lslife-backend
npx prisma generate
npm run build
pm2 restart all
