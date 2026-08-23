import crypto from 'node:crypto';

const SECRET_SEED = process.env.CHAT_SECRET_KEY || process.env.JWT_SECRET || 'lslife-super-secret-default-key-32b';
// 导出 32 字节 (256 位) 的标准 AES 密钥
const ENCRYPTION_KEY = crypto.scryptSync(SECRET_SEED, 'lslife-salt-v1', 32);
const ALGORITHM = 'aes-256-gcm';
const PREFIX = 'ENC_GCM_V1:';

/**
 * 使用 AES-256-GCM 对聊天记录进行应用层静态加密
 */
export function encryptChatMessage(plaintext: string): string {
  if (!plaintext) return plaintext;
  try {
    const iv = crypto.randomBytes(12); // 96-bit standard IV for GCM
    const cipher = crypto.createCipheriv(ALGORITHM, ENCRYPTION_KEY, iv);
    let encrypted = cipher.update(plaintext, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    const authTag = cipher.getAuthTag().toString('hex');
    return `${PREFIX}${iv.toString('hex')}:${authTag}:${encrypted}`;
  } catch (err) {
    console.error('[Crypto] Encrypt error:', err);
    return plaintext;
  }
}

/**
 * 解密聊天记录；若非加密格式则直接返回原文本 (兼容历史存量与系统提示)
 */
export function decryptChatMessage(cipherString: string): string {
  if (!cipherString || !cipherString.startsWith(PREFIX)) {
    return cipherString;
  }
  try {
    const parts = cipherString.slice(PREFIX.length).split(':');
    if (parts.length !== 3) return '[消息格式异常]';
    const [ivHex, authTagHex, encryptedHex] = parts;
    const iv = Buffer.from(ivHex, 'hex');
    const authTag = Buffer.from(authTagHex, 'hex');
    const decipher = crypto.createDecipheriv(ALGORITHM, ENCRYPTION_KEY, iv);
    decipher.setAuthTag(authTag);
    let decrypted = decipher.update(encryptedHex, 'hex', 'utf8');
    decrypted += decipher.final('utf8');
    return decrypted;
  } catch (err) {
    console.error('[Crypto] Decrypt error or integrity check failed:', err);
    return '[加密对话已受损或被非法篡改]';
  }
}

/**
 * 计算密码学防篡改存证链哈希 (SHA-256 Hash Chain)
 * 将当前会话ID、发信人、时间戳、原始明文与上一条消息的证据哈希进行绑定计算
 */
export function generateEvidenceHash(
  sessionId: string,
  senderId: string,
  timestamp: string | Date,
  plaintext: string,
  prevHash?: string | null,
  mediaHash?: string | null
): string {
  const tsStr = timestamp instanceof Date ? timestamp.toISOString() : String(timestamp);
  const payload = `${sessionId}|${senderId}|${tsStr}|${plaintext}|${mediaHash || 'NO_MEDIA'}|${prevHash || 'GENESIS_BLOCK'}`;
  return crypto.createHash('sha256').update(payload, 'utf8').digest('hex');
}
