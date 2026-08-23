import { Router } from 'express';
import multer from 'multer';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { v4 as uuidv4 } from 'uuid';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, requireAdminAuth } from '../middleware/auth.js';

const router = Router();

const __dirname = path.dirname(fileURLToPath(import.meta.url));
/** 编译后 dist/modules → 项目根 public/uploads */
const UPLOAD_DIR = path.resolve(__dirname, '../../public/uploads');
const CHAT_IMG_DIR = path.resolve(__dirname, '../../public/chat_imgs');
const CHAT_AUDIO_DIR = path.resolve(__dirname, '../../public/chat_audio');
fs.mkdirSync(UPLOAD_DIR, { recursive: true });
fs.mkdirSync(CHAT_IMG_DIR, { recursive: true });
fs.mkdirSync(CHAT_AUDIO_DIR, { recursive: true });

function publicBaseUrl(req: { protocol: string; get: (h: string) => string | undefined }): string {
  if (process.env.PUBLIC_BASE_URL) return process.env.PUBLIC_BASE_URL.replace(/\/$/, '');
  const proto = (req.get('x-forwarded-proto') || req.protocol || 'https').split(',')[0].trim();
  const host = req.get('x-forwarded-host') || req.get('host') || 'localhost:4000';
  return `${proto}://${host}`;
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, UPLOAD_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `${uuidv4()}${ext}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (file.mimetype.startsWith('image/')) cb(null, true);
    else cb(new ApiError(400, '只允许上传图片文件'));
  },
});

const chatAudioStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, CHAT_AUDIO_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || '.m4a';
    cb(null, `${uuidv4()}${ext}`);
  },
});

const chatAudioUpload = multer({
  storage: chatAudioStorage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB limit for audio
  fileFilter: (_req, file, cb) => {
    if (file.mimetype.startsWith('audio/')) cb(null, true);
    else cb(new ApiError(400, '只允许上传音频文件'));
  },
});

const chatImageStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, CHAT_IMG_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `${uuidv4()}${ext}`);
  },
});

const chatImageUpload = multer({
  storage: chatImageStorage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (file.mimetype.startsWith('image/')) cb(null, true);
    else cb(new ApiError(400, '只允许上传图片文件'));
  },
});

router.post(
  '/',
  requireAuth,
  upload.single('image'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的图片文件');
    const url = `${publicBaseUrl(req)}/uploads/${req.file.filename}`;
    return ok(res, { url });
  }),
);

import crypto from 'node:crypto';

router.post(
  '/upload-image',
  requireAuth,
  chatImageUpload.single('image'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的图片文件');
    
    // Calculate SHA-256 for the uploaded file
    const fileBuffer = fs.readFileSync(req.file.path);
    const hash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
    const url = `${publicBaseUrl(req)}/chat_imgs/${req.file.filename}`;
    return ok(res, { url, mediaHash: hash });
  }),
);

router.post(
  '/admin',
  requireAdminAuth,
  upload.single('image'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的图片文件');
    const url = `${publicBaseUrl(req)}/uploads/${req.file.filename}`;
    return ok(res, { url });
  }),
);

router.post(
  '/batch',
  requireAuth,
  upload.array('images', 9),
  asyncHandler(async (req, res) => {
    const files = req.files as Express.Multer.File[] | undefined;
    if (!files?.length) throw new ApiError(400, '未找到上传的图片文件');
    const base = publicBaseUrl(req);
    const urls = files.map((f) => `${base}/uploads/${f.filename}`);
    return ok(res, { urls });
  }),
);

router.post(
  '/audio',
  requireAuth,
  chatAudioUpload.single('audio'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的音频文件');
    
    // Calculate SHA-256 for the uploaded audio file
    const fileBuffer = fs.readFileSync(req.file.path);
    const hash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
    const url = `${publicBaseUrl(req)}/chat_audio/${req.file.filename}`;
    return ok(res, { url, mediaHash: hash });
  }),
);

// ==================== APK 上传（管理员专用）====================
const APK_DIR = path.resolve(__dirname, '../../public/apks');
fs.mkdirSync(APK_DIR, { recursive: true });

const apkStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, APK_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || '.apk';
    cb(null, `lslife_${Date.now()}${ext}`);
  },
});

const apkUpload = multer({
  storage: apkStorage,
  limits: { fileSize: 150 * 1024 * 1024 }, // 150MB
  fileFilter: (_req, file, cb) => {
    const isApk =
      file.mimetype === 'application/vnd.android.package-archive' ||
      file.mimetype === 'application/octet-stream' ||
      file.originalname.toLowerCase().endsWith('.apk');
    if (isApk) cb(null, true);
    else cb(new ApiError(400, '只允许上传 APK 文件'));
  },
});

router.post(
  '/apk',
  requireAdminAuth,
  apkUpload.single('apk'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的 APK 文件');
    // 计算 MD5
    const fileBuffer = fs.readFileSync(req.file.path);
    const md5 = crypto.createHash('md5').update(fileBuffer).digest('hex');
    const url = `${publicBaseUrl(req)}/apks/${req.file.filename}`;
    return ok(res, {
      url,
      fileSize: req.file.size,
      md5,
      filename: req.file.filename,
    });
  }),
);

export default router;

