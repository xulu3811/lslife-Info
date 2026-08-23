import sys
import re

with open('deploy_tmp/backend/prisma/schema.prisma', 'r', encoding='utf-8') as f:
    schema = f.read()

# Add User fields
user_replacement = '''
  membershipTier  String    @default("regular") // regular | vip | premium
  vipExpireAt     DateTime?
  role            String    @default("USER_PERSONAL") // USER_PERSONAL | MERCHANT_VERIFIED | ADMIN
  freeQuota       Int       @default(3)
  paidQuota       Int       @default(0)
'''
schema = re.sub(r'  membershipTier  String    @default\(\"regular\"\).*?\n', user_replacement, schema)

# Ensure User has the new relations
user_relations = '''
  ownedMerchants  Merchant[] @relation("MerchantOwner")
  merchantInfo    MerchantInfo?
  quotaLogs       QuotaLedger[]
  billingOrders   BillingOrder[]
  valueAddedLogs  ValueAddedLog[]
'''
schema = re.sub(r'  ownedMerchants\s+Merchant\[\]\s+@relation\(\"MerchantOwner\"\)\n', user_relations + '\n', schema, count=1)

# Add Post fields
post_replacement = '''
  topic            String?  // 新增：参与的同城话题 (如 #连山探店, #周末去哪儿)
  isTop            Boolean  @default(false)
  topExpireAt      DateTime?
  refreshedAt      DateTime?
  linkedCommerceId String?
'''
schema = re.sub(r'  topic\s+String\?.*?//.*?\n', post_replacement + '\n', schema, count=1)

post_relations = '''
  categoryRel Category?   @relation(fields: [categoryId], references: [id])
  favorites   Favorite[]
  valueAddedLogs ValueAddedLog[]
'''
schema = re.sub(r'  categoryRel\s+Category\?.*?@relation.*?\[id\]\)\n\s+favorites\s+Favorite\[\]\n', post_relations + '\n', schema, count=1)

# Add new models
new_models = '''
// ================= 商业化与配额体系 =================
model MerchantInfo {
  id            String   @id @default(cuid())
  userId        String   @unique
  shopName      String
  shopLogo      String?
  shopBanner    String?
  address       String
  latitude      Float?
  longitude     Float?
  contactPhone  String
  businessHours String?
  licenseUrl    String?
  verifyStatus  String   @default("PENDING") // PENDING, APPROVED, REJECTED
  createdAt     DateTime @default(now())
  updatedAt     DateTime @updatedAt

  user          User     @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model BillingOrder {
  id         String   @id @default(cuid())
  userId     String
  type       String   // POST_QUOTA | MERCHANT_VERIFY | POST_TOP | POST_REFRESH
  amount     Int      // in cents
  status     String   @default("PENDING") // PENDING | PAID | CANCELLED
  payChannel String   @default("WECHAT") // WECHAT | ALIPAY | MOCK_WALLET
  createdAt  DateTime @default(now())
  updatedAt  DateTime @updatedAt

  user       User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  
  @@index([userId])
}

model QuotaLedger {
  id           String   @id @default(cuid())
  userId       String
  changeAmount Int      // Positive for added, negative for consumed
  reason       String   // BUY_QUOTA, NEW_USER_FREE, POST_CONSUME
  createdAt    DateTime @default(now())

  user         User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  
  @@index([userId])
}

model ValueAddedLog {
  id        String   @id @default(cuid())
  userId    String
  postId    String
  action    String   // TOP, REFRESH
  cost      Int      // Cost in cents
  createdAt DateTime @default(now())

  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  post      Post     @relation(fields: [postId], references: [id], onDelete: Cascade)
  
  @@index([userId])
  @@index([postId])
}
'''
schema += '\n' + new_models

# Ensure Postgres provider
schema = schema.replace('provider = "sqlite"', 'provider = "postgresql"')

with open('backend/prisma/schema.prisma', 'w', encoding='utf-8') as f:
    f.write(schema)
with open('schema.prisma', 'w', encoding='utf-8') as f:
    f.write(schema)
