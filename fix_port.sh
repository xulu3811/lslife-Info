#!/bin/bash
cd ~/lslife-backend
sed -i 's/127.0.0.1:5433/127.0.0.1:5432/g' .env
export PATH="$HOME/.local/nodejs/bin:$PATH"
pm2 restart all
