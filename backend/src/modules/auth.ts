import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { signToken } from '../lib/jwt.js';
import { createHash } from 'node:crypto';
import { triggerProfileAiReview } from '../services/moderation.js';
import Tesseract from 'tesseract.js';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const router = Router();

const phoneSchema = z.string().regex(/^1\d{10}$/, '请输入正确的11位手机号');
const passwordSchema = z.string()
  .min(6, '密码至少6位')
  .max(64, '密码过长')
  .regex(/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, '密码要求6位以上数字+英文字母');

const DEFAULT_AVATAR =
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80';

/**
 * 提取并校验设备指纹
 */
async function checkDeviceFingerprint(req: any, userId: string) {
  const deviceId = req.headers['x-device-id'] as string;
  if (!deviceId) return; // 兼容旧版本

  const risk = req.headers['x-device-risk'] as string;
  
  // 查找或创建 DeviceFingerprint
  let device = await prisma.deviceFingerprint.findUnique({ where: { deviceId } });
  if (!device) {
    device = await prisma.deviceFingerprint.create({ data: { deviceId } });
  }

  if (device.isBanned) {
    throw new ApiError(403, '该设备已被封禁');
  }

  // 统计该设备关联的账号数
  const logins = await prisma.userDeviceLogin.findMany({ where: { deviceId }, select: { userId: true } });
  const uniqueUsers = new Set(logins.map(l => l.userId));
  
  if (uniqueUsers.size >= 3 && !uniqueUsers.has(userId)) {
    throw new ApiError(403, '该设备绑定的账号已达上限');
  }

  // 记录登录
  await prisma.userDeviceLogin.upsert({
    where: { userId_deviceId: { userId, deviceId } },
    create: { userId, deviceId },
    update: { lastLoginAt: new Date() }
  });
}

/**
 * 注册（手机号 + 密码）
 * - 新号：创建账号
 * - 历史短信用户（无密码）：补设密码后直接登录
 * - 已设密码：提示去登录
 */
router.post(
  '/register',
  asyncHandler(async (req, res) => {
    const { phone, email, password, nickname } = z
      .object({
        phone: phoneSchema,
        email: z.string().email('请输入有效的邮箱').optional(),
        password: passwordSchema,
        nickname: z.string().min(1).max(20).optional(),
      })
      .parse(req.body);

    const passwordHash = await bcrypt.hash(password, 10);
    const existing = await prisma.user.findUnique({ where: { phone } });

    if (email) {
      const emailUser = await prisma.user.findUnique({ where: { email } });
      if (emailUser && emailUser.phone !== phone) {
        throw new ApiError(409, '该邮箱已被其他账号绑定，请使用其他邮箱');
      }
    }

    if (existing?.passwordHash) {
      throw new ApiError(409, '该手机号已注册，请直接登录');
    }

    let user;
    if (existing) {
      user = await prisma.user.update({
        where: { id: existing.id },
        data: {
          passwordHash,
          email: email || existing.email,
          pendingNickname: nickname || existing.nickname,
          profileReviewStatus: 'AI_REVIEWING',
        },
      });
      triggerProfileAiReview(user.id, user.pendingNickname).catch(console.error);
    } else {
      const defaultNickname = `清远新用户${phone.slice(-4)}`;
      user = await prisma.user.create({
        data: {
          phone,
          email,
          passwordHash,
          nickname: defaultNickname,
          avatar: DEFAULT_AVATAR,
          pendingNickname: nickname || defaultNickname,
          pendingAvatar: DEFAULT_AVATAR,
          profileReviewStatus: 'AI_REVIEWING',
        },
      });
      triggerProfileAiReview(user.id, user.pendingNickname).catch(console.error);
    }
    
    await checkDeviceFingerprint(req, user.id);

    const token = signToken({ sub: user.id, phone: user.phone });
    return ok(res, { token, user: sanitize(user) }, '注册成功');
  }),
);

/**
 * 登录（手机号 + 密码）
 * 个人开发阶段暂不接入短信，短信接口仅作兼容保留。
 */
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        phone: phoneSchema,
        password: passwordSchema.optional(),
        // 兼容旧客户端：若仍传 code 则明确提示升级
        code: z.string().optional(),
      })
      .parse(req.body);

    if (body.code && !body.password) {
      throw new ApiError(400, '已改为密码登录，请升级 App 后使用手机号+密码');
    }
    if (!body.password) {
      throw new ApiError(400, '请输入密码');
    }

    const user = await prisma.user.findUnique({ where: { phone: body.phone } });
    if (!user) {
      throw new ApiError(401, '未注册的手机号');
    }
    if (!user.passwordHash) {
      throw new ApiError(400, '该账号尚未设置密码，请先注册');
    }

    const okPwd = await bcrypt.compare(body.password, user.passwordHash);
    if (!okPwd) {
      throw new ApiError(401, '密码错误');
    }
    
    await checkDeviceFingerprint(req, user.id);

    const token = signToken({ sub: user.id, phone: user.phone });
    return ok(res, { token, user: sanitize(user) }, '登录成功');
  }),
);

/** @deprecated 个人开发暂无法开通短信，保留接口避免旧客户端崩溃 */
router.post(
  '/send-code',
  asyncHandler(async () => {
    throw new ApiError(503, '暂未开通短信验证，请使用手机号+密码登录或注册');
  }),
);

/** 忘记密码：通过邮箱发送验证码 */
router.post(
  '/forgot-password/code',
  asyncHandler(async (req, res) => {
    const { email } = z.object({ email: z.string().email('邮箱格式不正确') }).parse(req.body);
    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) throw new ApiError(404, '该邮箱未绑定任何账号');

    const code = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15分钟有效期

    await prisma.verificationCode.create({
      data: { phone: email, code, purpose: 'reset_password', expiresAt } // 复用 phone 字段存储 email
    });

    // 模拟发送邮件
    console.log(`\n========================================`);
    console.log(`[Email Mock] 向 ${email} 发送密码找回验证码: ${code}`);
    console.log(`========================================\n`);

    return ok(res, null, '验证码已发送至您的邮箱');
  })
);

/** 忘记密码：重置密码 */
router.post(
  '/forgot-password/reset',
  asyncHandler(async (req, res) => {
    const { email, code, newPassword } = z.object({
      email: z.string().email('邮箱格式不正确'),
      code: z.string().length(6, '验证码必须是6位数字'),
      newPassword: passwordSchema
    }).parse(req.body);

    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) throw new ApiError(404, '该邮箱未绑定任何账号');

    const record = await prisma.verificationCode.findFirst({
      where: { phone: email, code, purpose: 'reset_password', consumed: false },
      orderBy: { createdAt: 'desc' }
    });

    if (!record || record.expiresAt < new Date()) {
      throw new ApiError(400, '验证码无效或已过期');
    }

    const passwordHash = await bcrypt.hash(newPassword, 10);
    
    // 更新密码并核销验证码
    await prisma.$transaction([
      prisma.user.update({ where: { id: user.id }, data: { passwordHash } }),
      prisma.verificationCode.update({ where: { id: record.id }, data: { consumed: true } })
    ]);

    return ok(res, null, '密码重置成功');
  })
);

/** 当前用户信息 */
router.get(
  '/me',
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) {
        throw new ApiError(404, '用户不存在');
    }
    
    // Auto-upgrade hardcoded admin phones to SUPERADMIN
    const adminPhones = ['19926387658', '13828577665'];
    if (adminPhones.includes(user.phone) && user.role !== 'SUPERADMIN') {
        await prisma.user.update({ where: { id: user.id }, data: { role: 'SUPERADMIN' } });
        user.role = 'SUPERADMIN';
    }

    const favCount = await prisma.favorite.count({ where: { userId: user.id } });
    const fpCount = await prisma.footprint.count({ where: { userId: user.id } });
    return ok(res, sanitize({ ...user, favoritesCount: favCount, footprintsCount: fpCount, followingCount: user.followingCount, followersCount: user.followersCount }));
  }),
);

/** OCR 真实端点 (使用 Tesseract 识别身份证) */
router.post(
  '/realname/ocr',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { imageUrl } = z.object({ imageUrl: z.string().url('必须提供图片链接') }).parse(req.body);
    
    // 尝试转成本地路径以加快读取速度
    let imageTarget = imageUrl;
    try {
      const urlObj = new URL(imageUrl);
      if (urlObj.pathname.startsWith('/uploads/')) {
        const localPath = path.resolve(__dirname, '../../public', urlObj.pathname.substring(1));
        if (fs.existsSync(localPath)) {
          imageTarget = localPath;
        }
      }
    } catch (e) {
      // 忽略错误，直接使用原URL
    }

    try {
      const { data: { text } } = await Tesseract.recognize(
        imageTarget,
        'chi_sim',
        { logger: m => {} } // 静默日志
      );

      let realName = '';
      let idCard = '';

      // 提取姓名
      // 匹配 "姓名 许路", "名 许路" 考虑到OCR误差可能有空格
      const nameMatch = text.match(/姓\s*名[\s:]*([^\n\d]+)/);
      if (nameMatch) {
        realName = nameMatch[1].replace(/[\s\r]/g, '');
      }

      // 提取身份证号码
      // 匹配 18位数字或带X
      const idMatch = text.match(/(\d{17}[\dXx])/);
      if (idMatch) {
        idCard = idMatch[1];
      }

      if (!realName && !idCard) {
        return ok(res, { realName: '识别失败', idCard: '' }, '无法识别身份证信息');
      }

      return ok(res, {
        realName: realName || '未知',
        idCard: idCard || ''
      }, 'OCR识别成功');
    } catch (error) {
      console.error("OCR Error:", error);
      throw new ApiError(500, 'OCR 识别引擎异常');
    }
  }),
);

/** 实名认证 (KYC 强绑定与黑名单校验) */
router.post(
  '/realname',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { realName, idCard, idCardFrontImage, idCardBackImage } = z
      .object({ 
        realName: z.string().min(2), 
        idCard: z.string().regex(/^\d{17}[\dXx]$/, '身份证号格式错误'),
        idCardFrontImage: z.string().url('必须提供身份证正面照').optional(),
        idCardBackImage: z.string().url('必须提供身份证反面照').optional()
      })
      .parse(req.body);
    
    // KYC: 检查身份库
    let personIdentity = await prisma.personIdentity.findUnique({
      where: { idCardNumber: idCard },
      include: { users: true }
    });
    
    if (personIdentity) {
      if (personIdentity.isBlacklisted) {
        throw new ApiError(403, '该身份已被平台永久封禁，无法进行实名认证。');
      }
      const otherUsers = personIdentity.users.filter(u => u.id !== req.userId);
      if (otherUsers.length >= 2) {
        throw new ApiError(403, '该身份绑定的账号已达上限（最多2个）。');
      }
    } else {
      personIdentity = await prisma.personIdentity.create({
        data: { idCardNumber: idCard, realName },
        include: { users: true }
      });
    }

    const idCardHash = createHash('sha256').update(idCard).digest('hex');
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { 
        realName, 
        idCardHash, 
        idCardFrontImage,
        idCardBackImage,
        realNameStatus: 'pending', // 转入待审核状态
        personIdentityId: idCard
      },
    });
    return ok(res, sanitize(user), '实名认证资料已提交，请等待人工审核');
  }),
);

/** 更新昵称/头像 */
router.patch(
  '/profile',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { nickname, avatar } = z
      .object({ nickname: z.string().min(1).optional(), avatar: z.string().url().optional() })
      .parse(req.body);
    
    // 如果没有修改昵称和头像，直接返回
    if (!nickname && !avatar) {
      const u = await prisma.user.findUnique({ where: { id: req.userId! } });
      return ok(res, sanitize(u!), '无更新内容');
    }

    const dataToUpdate: any = {
      profileReviewStatus: 'AI_REVIEWING',
    };
    if (nickname) dataToUpdate.pendingNickname = nickname;
    if (avatar) dataToUpdate.pendingAvatar = avatar;

    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: dataToUpdate,
    });
    
    // 异步触发审核
    triggerProfileAiReview(user.id, nickname || null).catch(console.error);

    return ok(res, sanitize(user), '资料已提交，请等待系统及人工审核后生效');
  }),
);

/** 登录后修改密码 */
router.post(
  '/change-password',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { oldPassword, newPassword } = z
      .object({
        oldPassword: passwordSchema,
        newPassword: passwordSchema,
      })
      .parse(req.body);

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user?.passwordHash) throw new ApiError(400, '账号未设置密码');

    const match = await bcrypt.compare(oldPassword, user.passwordHash);
    if (!match) throw new ApiError(400, '原密码错误');

    const passwordHash = await bcrypt.hash(newPassword, 10);
    await prisma.user.update({ where: { id: user.id }, data: { passwordHash } });
    return ok(res, { changed: true }, '密码已修改');
  }),
);

function sanitize(user: { idCardHash?: string | null; passwordHash?: string | null } & Record<string, unknown>) {
  const { idCardHash, passwordHash, ...rest } = user;
  return rest;
}

export default router;
