import jwt from 'jsonwebtoken';
import { env } from '../config/env.js';

export interface JwtPayload {
  sub: string; // userId or adminId
  phone?: string;
  role?: string;
  isAdmin?: boolean;
}

export function signToken(payload: JwtPayload): string {
  return jwt.sign(payload, env.jwtSecret, { expiresIn: env.jwtExpiresIn as jwt.SignOptions['expiresIn'] });
}

export function verifyToken(token: string): JwtPayload {
  return jwt.verify(token, env.jwtSecret) as JwtPayload;
}
