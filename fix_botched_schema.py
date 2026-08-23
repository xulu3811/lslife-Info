import re

with open('backend/prisma/schema.prisma', 'r', encoding='utf-8') as f:
    content = f.read()

# We need to find the start of MerchantInfo
start_idx = content.find('model MerchantInfo {')
end_idx = content.find('model ValueAddedLog {')

if start_idx != -1 and end_idx != -1:
    models = """model MerchantInfo {
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
  metadata   Json?    // 存储订单的附加信息，例如 postId, days 等
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

"""
    new_content = content[:start_idx] + models + content[end_idx:]
    with open('backend/prisma/schema.prisma', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Fixed models.")
else:
    print("Could not find boundaries.")
