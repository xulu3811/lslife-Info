import type { Response } from 'express';

/** 统一响应结构: { code, message, data } */
export function ok<T>(res: Response, data: T, message = 'ok') {
  return res.json({ code: 0, message, data });
}

export function fail(res: Response, httpStatus: number, message: string, code = httpStatus) {
  return res.status(httpStatus).json({ code, message, data: null });
}

/** 业务异常, 由全局错误中间件捕获 */
export class ApiError extends Error {
  status: number;
  code: number;
  constructor(status: number, message: string, code?: number) {
    super(message);
    this.status = status;
    this.code = code ?? status;
  }
}
