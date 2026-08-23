import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { signToken } from '../lib/jwt.js';
import { createHash } from 'node:crypto';

const router = Router();

const phoneSchema = z.string().regex(/^1\d{10}$/, '请输入正确的11位手机号');
const passwordSchema = z.string()
  .min(6, '密码至少6位')
  .max(64, '密码过长')
  .regex(/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, '密码要求6位以上数字+英文字母');

const DEFAULT_AVATAR =
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80';

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
          nickname: nickname || existing.nickname,
        },
      });
    } else {
      user = await prisma.user.create({
        data: {
          phone,
          email,
          passwordHash,
          nickname: nickname || `连山用户${phone.slice(-4)}`,
          avatar: DEFAULT_AVATAR,
        },
      });
    }

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
    if (!user) throw new ApiError(404, '用户不存在');
    return ok(res, sanitize(user));
  }),
);

/** 实名认证 (存凭证并转入人工审核) */
router.post(
  '/realname',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { realName, idCard, idCardFrontImage, idCardBackImage, idCardHandheldImage } = z
      .object({ 
        realName: z.string().min(2), 
        idCard: z.string().regex(/^\d{17}[\dXx]$/, '身份证号格式错误'),
        idCardFrontImage: z.string().url('必须提供身份证正面照').optional(),
        idCardBackImage: z.string().url('必须提供身份证反面照').optional(),
        idCardHandheldImage: z.string().url('必须提供手持身份证照').optional()
      })
      .parse(req.body);
    const idCardHash = createHash('sha256').update(idCard).digest('hex');
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { 
        realName, 
        idCardHash, 
        idCardFrontImage,
        idCardBackImage,
        idCardHandheldImage,
        realNameStatus: 'pending' // 转入待审核状态
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
    const user = await prisma.user.update({ where: { id: req.userId! }, data: { nickname, avatar } });
    return ok(res, sanitize(user), '已更新');
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
