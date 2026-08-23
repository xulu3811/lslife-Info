const fs = require('fs');

let schema = fs.readFileSync('D:\\LsLife\\backend\\prisma\\schema.prisma', 'utf8');

// 1. Add fields to User
schema = schema.replace(
  'reports        Report[]',
  `reports        Report[]

  personIdentityId String?
  personIdentity   PersonIdentity? @relation(fields: [personIdentityId], references: [idCardNumber])
  devices          UserDeviceLogin[]`
);

// 2. Add new models
schema += `

model PersonIdentity {
  idCardNumber String @id
  realName     String
  isBlacklisted Boolean @default(false)
  createdAt    DateTime @default(now())
  updatedAt    DateTime @updatedAt
  
  users        User[]
}

model DeviceFingerprint {
  deviceId     String @id
  isBanned     Boolean @default(false)
  createdAt    DateTime @default(now())
  updatedAt    DateTime @updatedAt
  
  logins       UserDeviceLogin[]
}

model UserDeviceLogin {
  id           String @id @default(cuid())
  userId       String
  deviceId     String
  lastLoginAt  DateTime @default(now())
  
  user         User @relation(fields: [userId], references: [id], onDelete: Cascade)
  device       DeviceFingerprint @relation(fields: [deviceId], references: [deviceId], onDelete: Cascade)
  
  @@unique([userId, deviceId])
}
`;

// 3. Add fields to Post
schema = schema.replace(
  'linkedCommerceId String?',
  `linkedCommerceId String?

  textHash         String?  // 新增：用于文本去重
  imageHashes      String   @default("[]") // 新增：图片哈希数组，用于去重
  isShadowBanned   Boolean  @default(false) // 新增：是否被影子封禁 (不展示在 Feeds 中)`
);

// 4. Remove MOMENT from PostType
schema = schema.replace(
  `enum PostType {
  CLASSIFIED
  MOMENT
}`,
  `enum PostType {
  CLASSIFIED
}`
);

fs.writeFileSync('D:\\LsLife\\backend\\prisma\\schema.prisma', schema);
console.log('Schema updated successfully!');
