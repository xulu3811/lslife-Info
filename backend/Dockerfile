# 连山同城 LsLife 后端镜像
FROM node:20-alpine AS base
WORKDIR /app

FROM base AS deps
COPY package.json package-lock.json* ./
RUN npm install --omit=dev && npm install prisma --no-save

FROM base AS build
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npx prisma generate && npm run build

FROM base AS runtime
ENV NODE_ENV=production
COPY --from=deps /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist
COPY --from=build /app/node_modules/.prisma ./node_modules/.prisma
COPY prisma ./prisma
COPY package.json ./
EXPOSE 4000
# 生产: 启动前执行迁移 (prisma migrate deploy)
CMD ["sh", "-c", "npx prisma migrate deploy || npx prisma db push; node dist/index.js"]
