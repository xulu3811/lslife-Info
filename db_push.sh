#!/bin/bash
cd ~/lslife-backend
export PATH="$HOME/.local/nodejs/bin:$PATH"
npx prisma db push --accept-data-loss
pm2 restart all
