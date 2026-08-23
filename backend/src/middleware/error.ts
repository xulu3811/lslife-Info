import type { NextFunction, Request, Response } from 'express';
import { ZodError } from 'zod';
import { ApiError } from '../lib/http.js';

export function notFound(_req: Request, res: Response) {
  res.status(404).json({ code: 404, message: '接口不存在', data: null });
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function errorHandler(err: unknown, _req: Request, res: Response, _next: NextFunction) {
  if (err instanceof ApiError) {
    return res.status(err.status).json({ code: err.code, message: err.message, data: null });
  }
  if (err instanceof ZodError) {
    const message = err.errors.map((e) => `${e.path.join('.')}: ${e.message}`).join('; ');
    return res.status(400).json({ code: 400, message: `参数错误: ${message}`, data: null });
  }
  console.error('[UnhandledError]', err);
  return res.status(500).json({ code: 500, message: '服务器内部错误', data: null });
}

/** 包装 async 路由, 统一抛错到 errorHandler */
export function asyncHandler<T extends (req: Request, res: Response, next: NextFunction) => Promise<unknown>>(fn: T) {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}
