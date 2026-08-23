import React, { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import { 
  ChevronRight, 
  PackageSearch,
  Clock,
  CheckCircle2,
  Bike,
  Store,
  RefreshCw,
  ShoppingBag
} from 'lucide-react';
import { Order } from '../types';

interface OrderListViewProps {
  isDarkMode?: boolean;
  onNavigateToHome: () => void;
  onNavigateToTracker: (orderId: string) => void;
  onNavigateToDetail: (merchantId: string) => void; // To buy again
}

export default function OrderListView({
  isDarkMode = false,
  onNavigateToHome,
  onNavigateToTracker,
  onNavigateToDetail
}: OrderListViewProps) {
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchOrders = async () => {
    try {
      const res = await fetch('/api/orders');
      if (res.ok) {
        const data = await res.json();
        setOrders(data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchOrders();
  };

  const getStatusBadge = (status: string) => {
    switch(status) {
      case 'preparing':
        return (
          <span className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400 text-[10px] font-bold border border-amber-500/20">
            <Clock size={12} className="animate-pulse" />
            商家备餐中
          </span>
        );
      case 'delivering':
        return (
          <span className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-500/10 text-red-600 dark:text-red-400 text-[10px] font-bold border border-red-500/20">
            <Bike size={12} className="animate-bounce" />
            骑手派送中
          </span>
        );
      case 'delivered':
        return (
          <span className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 text-[10px] font-bold border border-gray-200 dark:border-gray-700">
            <CheckCircle2 size={12} />
            订单已送达
          </span>
        );
      default:
        return null;
    }
  };

  const formatTime = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="flex flex-col h-full bg-gray-50 dark:bg-black transition-colors duration-300">
        <div className="px-5 py-4 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800 shrink-0">
          <h2 className="text-[17px] font-extrabold tracking-tight text-gray-900 dark:text-white">全部订单</h2>
        </div>
        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="w-10 h-10 border-4 border-red-500/20 border-t-blue-500 rounded-full animate-spin mb-4" />
          <p className="text-sm font-medium text-gray-400">正在加载订单数据...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-gray-50 dark:bg-black transition-colors duration-300">
      {/* Header */}
      <div className="px-5 pt-5 pb-3 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800 shrink-0 flex items-center justify-between sticky top-0 z-20 shadow-sm">
        <h2 className="text-[22px] font-extrabold tracking-tight text-gray-900 dark:text-white">全部订单</h2>
        <button 
          onClick={handleRefresh}
          className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-500 dark:text-gray-400 transition"
        >
          <RefreshCw size={18} className={refreshing ? 'animate-spin' : ''} />
        </button>
      </div>

      {/* Orders List */}
      <div className="flex-1 overflow-y-auto px-3 py-3 md:px-4">
        <div className="max-w-2xl mx-auto flex flex-col gap-4 pb-24">
          {orders.length === 0 ? (
            <div className="py-24 mt-8 flex flex-col items-center justify-center bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-[2rem] shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)] p-4">
              <div className="w-20 h-20 rounded-[20px] bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-800/50 flex items-center justify-center mb-5 text-gray-400">
                <PackageSearch size={32} strokeWidth={2.5} />
              </div>
              <p className="text-[15px] font-black tracking-tight text-gray-900 dark:text-white">暂无订单记录</p>
              <p className="text-xs font-medium text-gray-500 dark:text-gray-400 mt-2 text-center max-w-[200px]">
                您还没有下过单，快去首页看看有什么好吃的吧！
              </p>
              <button
                onClick={onNavigateToHome}
                className="mt-6 px-8 py-3 rounded-2xl bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white text-[13px] font-bold shadow-[0_8px_20px_rgba(59,130,246,0.3)] transition active:scale-95 flex items-center gap-1.5"
              >
                去逛逛
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {orders.map((order, idx) => {
                const isOngoing = order.status === 'preparing' || order.status === 'delivering';
                
                return (
                  <motion.div
                    key={order.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                    className="bg-white dark:bg-gray-900 rounded-[1.5rem] border border-gray-100 dark:border-gray-800 overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)] relative"
                  >
                    {/* Interactive overlay for ongoing orders */}
                    {isOngoing && (
                      <div className="absolute inset-x-0 top-0 h-1 bg-gradient-to-r from-red-400 to-indigo-500" />
                    )}

                    <div className="p-4 sm:p-4">
                      {/* Merchant & Status Header */}
                      <div className="flex items-center justify-between mb-4">
                        <div 
                          className="flex items-center gap-2 cursor-pointer group"
                          onClick={() => onNavigateToDetail(order.merchantId)}
                        >
                          <img 
                            src={order.merchantLogo} 
                            alt={order.merchantName} 
                            className="w-8 h-8 rounded-lg object-cover bg-gray-50 dark:bg-gray-800 border border-gray-100 dark:border-gray-800"
                            referrerPolicy="no-referrer"
                          />
                          <div className="flex items-center gap-1">
                            <span className="font-extrabold text-[15px] text-gray-900 dark:text-white group-hover:text-red-500 transition-colors">
                              {order.merchantName}
                            </span>
                            <ChevronRight size={14} className="text-gray-400 group-hover:text-red-500 transition-colors" />
                          </div>
                        </div>
                        {getStatusBadge(order.status)}
                      </div>

                      {/* Items Preview */}
                      <div 
                        className="cursor-pointer"
                        onClick={() => {
                          if (isOngoing) {
                            onNavigateToTracker(order.id);
                          }
                        }}
                      >
                        <div className="flex gap-3 overflow-hidden pb-1 mb-3">
                          {order.items.slice(0, 4).map((item: any, itemIdx: number) => (
                            <div key={itemIdx} className="shrink-0 relative">
                              <img 
                                src={item.image} 
                                alt={item.name}
                                className="w-16 h-16 rounded-[10px] object-cover bg-gray-50 dark:bg-gray-800 border border-gray-100 dark:border-gray-800"
                                referrerPolicy="no-referrer"
                              />
                              {item.quantity > 1 && (
                                <span className="absolute -top-1.5 -right-1.5 bg-gray-900/80 dark:bg-black/80 backdrop-blur-sm text-white text-[9px] font-black w-5 h-5 flex items-center justify-center rounded-full ring-2 ring-white dark:ring-gray-900">
                                  x{item.quantity}
                                </span>
                              )}
                            </div>
                          ))}
                          {order.items.length > 4 && (
                            <div className="w-16 h-16 rounded-[10px] bg-gray-50 dark:bg-gray-800 border border-gray-100 dark:border-gray-800 flex items-center justify-center flex-col text-gray-500 shrink-0">
                              <span className="text-xs font-black">+{order.items.length - 4}</span>
                              <span className="text-[9px] font-bold">件商品</span>
                            </div>
                          )}
                        </div>

                        {/* Order Summary & Meta */}
                        <div className="flex items-end justify-between">
                          <div>
                            <p className="text-[11px] font-medium text-gray-400 font-mono mb-1">
                              {formatTime(order.createdAt)}
                            </p>
                            <p className="text-[11px] text-gray-400 font-medium">
                              总计 {order.items.reduce((sum: number, item: any) => sum + item.quantity, 0)} 件
                            </p>
                          </div>
                          <div className="text-right">
                            <span className="text-[11px] font-bold text-gray-500 dark:text-gray-400 mr-1">实付</span>
                            <span className="text-base font-black text-gray-900 dark:text-white font-mono tracking-tight">
                              ¥{order.totalAmount.toFixed(2)}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Action Buttons */}
                      <div className="mt-4 pt-4 border-t border-gray-50 dark:border-gray-800/60 flex items-center justify-end gap-2.5">
                        <button
                          onClick={() => onNavigateToDetail(order.merchantId)}
                          className="px-4 py-1.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300 text-[12px] font-bold transition-colors shadow-sm"
                        >
                          再来一单
                        </button>
                        {isOngoing ? (
                          <button
                            onClick={() => onNavigateToTracker(order.id)}
                            className="px-4 py-1.5 rounded-xl bg-gradient-to-r from-red-500 to-red-600 text-white text-[12px] font-bold shadow-md hover:shadow-lg transition-shadow"
                          >
                            查看配送状态
                          </button>
                        ) : (
                          <button
                            className="px-4 py-1.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300 text-[12px] font-bold transition-colors shadow-sm"
                          >
                            评价订单
                          </button>
                        )}
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
