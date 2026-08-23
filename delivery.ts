import { prisma } from '../lib/prisma.js';

export interface LiveDelivery {
  status: 'preparing' | 'delivering' | 'delivered';
  progress: number;
  secondsRemaining: number;
  rider: { name: string; phone: string; avatar: string; lat: number; lng: number };
}

/**
 * 真实配送数据: 返回数据库中由骑手上报的最新位置信息。
 */
export async function computeLiveDelivery(orderId: string): Promise<LiveDelivery | null> {
  const delivery = await prisma.delivery.findUnique({
    where: { orderId }
  });

  if (!delivery) return null;

  return {
    status: delivery.status as 'preparing' | 'delivering' | 'delivered',
    progress: delivery.progress,
    secondsRemaining: 0, // 真实环境中可接入高德/百度地图 API 计算 ETA，MVP先置0
    rider: { 
      name: delivery.riderName, 
      phone: delivery.riderPhone, 
      avatar: delivery.riderAvatar || '', 
      lat: delivery.riderLat, 
      lng: delivery.riderLng 
    },
  };
}
