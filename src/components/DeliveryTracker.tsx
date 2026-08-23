/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import { 
  Bike, 
  MapPin, 
  PhoneCall, 
  MessageSquare, 
  Clock, 
  Sparkles, 
  ShieldCheck, 
  CheckCircle2, 
  ChevronLeft,
  Navigation,
  Star,
  ThumbsUp
} from 'lucide-react';
import { Order, Merchant } from '../types';
import MapContainer from './MapContainer';

interface RiderReview {
  rating: number;
  tags: string[];
  comment: string;
  submittedAt: string;
}

interface DeliveryTrackerProps {
  orderId: string;
  isDarkMode?: boolean;
  onBackToHome: () => void;
}

export default function DeliveryTracker({
  orderId,
  isDarkMode = false,
  onBackToHome,
}: DeliveryTrackerProps) {
  const [order, setOrder] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [mockMerchants, setMockMerchants] = useState<Merchant[]>([]);

  // Rider feedback states
  const [review, setReview] = useState<RiderReview | null>(() => {
    const saved = localStorage.getItem(`rider_review_${orderId}`);
    return saved ? JSON.parse(saved) : null;
  });
  const [rating, setRating] = useState<number>(5);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [comment, setComment] = useState<string>('');
  const [hoveredRating, setHoveredRating] = useState<number | null>(null);
  const [showSuccessToast, setShowSuccessToast] = useState<boolean>(false);

  const handleRatingChange = (newRating: number) => {
    setRating(newRating);
    setSelectedTags([]);
  };

  const toggleTag = (tag: string) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags(selectedTags.filter(t => t !== tag));
    } else {
      setSelectedTags([...selectedTags, tag]);
    }
  };

  const handleSubmitReview = () => {
    const newReview: RiderReview = {
      rating,
      tags: selectedTags,
      comment: comment.trim(),
      submittedAt: new Date().toISOString()
    };
    localStorage.setItem(`rider_review_${orderId}`, JSON.stringify(newReview));
    setReview(newReview);
    setShowSuccessToast(true);
    setTimeout(() => setShowSuccessToast(false), 3000);
  };

  const getRatingLabel = (val: number) => {
    switch (val) {
      case 5: return '超级满意，推荐给大家！';
      case 4: return '非常满意，配送很棒！';
      case 3: return '一般，还有改进空间';
      case 2: return '不太满意，服务需要提升';
      case 1: return '非常差，服务态度很不友好';
      default: return '';
    }
  };

  const fetchOrderDetails = async () => {
    try {
      const res = await fetch(`/api/orders/${orderId}`);
      if (!res.ok) {
        throw new Error('无法获取订单状态');
      }
      const data = await res.json();
      setOrder(data);
    } catch (err: any) {
      setError(err.message || '获取订单出错');
    } finally {
      setLoading(false);
    }
  };

  // Seed local merchants for the map display
  const fetchMerchants = async () => {
    try {
      const res = await fetch('/api/merchants');
      if (res.ok) {
        const data = await res.json();
        setMockMerchants(data);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchMerchants();
    fetchOrderDetails();

    // Poll the server for delivery coordinate updates and state changes every 2 seconds
    const interval = setInterval(() => {
      fetchOrderDetails();
    }, 2000);

    return () => clearInterval(interval);
  }, [orderId]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[500px] p-6 text-center">
        <div className="relative w-12 h-12 mb-4">
          <div className="absolute inset-0 rounded-full border-4 border-red-500/10" />
          <div className="absolute inset-0 rounded-full border-4 border-t-blue-500 animate-spin" />
        </div>
        <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">
          正在加载实时配送雷达数据...
        </p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] p-6 text-center">
        <div className="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/20 text-red-500 flex items-center justify-center mb-4">
          <ShieldCheck size={24} />
        </div>
        <p className="font-bold text-gray-800 dark:text-gray-100">配送数据加载失败</p>
        <p className="text-xs text-gray-400 mt-1">{error || '未找到该订单'}</p>
        <button
          onClick={onBackToHome}
          className="mt-6 px-6 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-semibold text-xs shadow-md"
        >
          返回首页
        </button>
      </div>
    );
  }

  // Determine stage text and icon
  const getStatusDetails = () => {
    switch (order.status) {
      case 'preparing':
        return {
          title: '商家正在出餐中',
          desc: '连山美味正在烹制，马上交由美团专送骑手',
          color: 'text-amber-500 dark:text-amber-400',
          bgColor: 'bg-amber-500/10',
          eta: '约 10 分钟内送达'
        };
      case 'delivering':
        return {
          title: '骑手正在玩命配送中',
          desc: '骑手阿力已接到您的美味，正飞奔向您的小区',
          color: 'text-green-500 dark:text-green-400',
          bgColor: 'bg-green-500/10',
          eta: `预计 ${order.secondsRemaining || 20} 秒内送达`
        };
      case 'delivered':
        return {
          title: '美味已送达',
          desc: '骑手已安全将包裹递交，祝您用餐愉快！',
          color: 'text-red-500 dark:text-red-400',
          bgColor: 'bg-red-500/10',
          eta: '已妥投'
        };
      default:
        return {
          title: '订单处理中',
          desc: '正在确认您的支付明细...',
          color: 'text-gray-500',
          bgColor: 'bg-gray-100',
          eta: '正在计算...'
        };
    }
  };

  const statusInfo = getStatusDetails();

  return (
    <div className="flex flex-col h-full bg-gray-50 dark:bg-black transition-colors duration-300">
      {/* Mini App Bar */}
      <div className="flex items-center justify-between px-4 py-3 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800 shrink-0">
        <button
          onClick={onBackToHome}
          className="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-300 transition"
        >
          <ChevronLeft size={20} />
        </button>
        <div className="text-center">
          <h3 className="font-extrabold text-sm text-gray-900 dark:text-white">配送实时追踪</h3>
          <p className="text-[10px] text-gray-400 font-mono mt-0.5">订单号: {order.id}</p>
        </div>
        <span className="w-8" /> {/* Balance spacer */}
      </div>

      {/* Main Grid View */}
      <div className="flex-1 flex flex-col overflow-hidden relative bg-gray-50 dark:bg-black">
        
        {/* Left column / Top Panel: Map View */}
        <div className="flex-shrink-0 h-[46vh] relative z-0">
          <MapContainer
            merchants={mockMerchants}
            userLocation={{ lat: 24.4720, lng: 112.0810, name: '瑶香苑' }}
            selectedMerchantId={order.merchantId}
            activeRiderLocation={order.rider}
            riderStatus={order.status}
            isDarkMode={isDarkMode}
          />
        </div>

        {/* Right column / Bottom Panel: Delivery Info Dashboard */}
        <div className="flex-1 overflow-y-auto px-3 pb-3 pt-0 flex flex-col gap-2.5 relative z-10 -mt-8 rounded-t-[32px] bg-gray-50 dark:bg-black shadow-[0_-10px_30px_0_rgba(0,0,0,0.1)] dark:shadow-[0_-10px_30px_0_rgba(0,0,0,0.5)]">
          <div className="w-12 h-1.5 bg-gray-200 dark:bg-gray-800 rounded-full mx-auto my-3 shrink-0" />
          
          {/* Status Header Block */}
          <div className="p-3 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm flex flex-col gap-2">
            <div className="flex items-start justify-between">
              <div>
                <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold ${statusInfo.bgColor} ${statusInfo.color}`}>
                  <Bike size={13} className="animate-bounce" />
                  {order.status === 'preparing' ? '商家备餐中' : order.status === 'delivering' ? '正在派送' : '送达完成'}
                </span>
                <h2 className="text-[16px] font-extrabold tracking-tight mt-1.5 text-gray-900 dark:text-white">
                  {statusInfo.title}
                </h2>
              </div>
              
              {/* Countdown ETA */}
              <div className="text-right flex flex-col items-end">
                <Clock size={18} className="text-red-500 animate-pulse" />
                <span className="text-xs font-bold text-gray-800 dark:text-gray-200 mt-1 font-mono">
                  {statusInfo.eta}
                </span>
              </div>
            </div>

            <p className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed border-t border-gray-50 dark:border-gray-800/60 pt-2">
              {statusInfo.desc}
            </p>

            {/* Delivery progress bar */}
            <div className="mt-2">
              <div className="flex justify-between text-[10px] text-gray-400 mb-1 font-medium">
                <span>商家接单</span>
                <span>骑手送达</span>
              </div>
              <div className="w-full h-2 rounded-full bg-gray-100 dark:bg-gray-800 overflow-hidden">
                <div 
                  className="h-full bg-gradient-to-r from-red-500 to-green-500 rounded-full transition-all duration-1000"
                  style={{ width: `${order.progress}%` }}
                />
              </div>
            </div>
          </div>

          {/* Rider contact block */}
          <div className="p-2.5 px-3 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm flex items-center justify-between">
            <div className="flex items-center gap-3">
              <img
                src={order.rider?.avatar}
                alt="Rider"
                referrerPolicy="no-referrer"
                className="w-10 h-10 rounded-full object-cover border-2 border-green-400 shadow"
              />
              <div>
                <div className="flex items-center gap-1">
                  <span className="font-bold text-sm text-gray-900 dark:text-white">{order.rider?.name}</span>
                  <span className="bg-green-500/10 text-green-600 dark:text-green-400 text-[9px] font-black px-1.5 py-0.5 rounded">
                    金牌骑手
                  </span>
                </div>
                <p className="text-xs text-gray-400 mt-0.5">美团专送 · 连山城市广场站</p>
              </div>
            </div>

            {/* Communication tools */}
            <div className="flex gap-2">
              <a 
                href={`tel:${order.rider?.phone}`}
                className="p-2 rounded-xl bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-750 text-red-500 transition"
                title="电话联系"
              >
                <PhoneCall size={14} />
              </a>
              <button 
                onClick={() => alert('已开启临时安全对话，骑手正在行车中，请注意行车安全。')}
                className="p-2 rounded-xl bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-750 text-gray-600 dark:text-gray-300 transition"
                title="同城发送消息"
              >
                <MessageSquare size={14} />
              </button>
            </div>
          </div>

          {/* Rider rating and service evaluation component (when order is delivered) */}
          {order.status === 'delivered' && (
            <div className="p-4 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm flex flex-col gap-3">
              {!review ? (
                <>
                  <div className="flex items-center justify-between border-b border-gray-50 dark:border-gray-800/50 pb-2.5">
                    <span className="font-extrabold text-xs text-gray-400 uppercase tracking-wider">骑手服务评价</span>
                    <span className="text-[10px] text-red-500 font-extrabold bg-red-500/10 px-2 py-0.5 rounded-full flex items-center gap-1">
                      <Sparkles size={10} className="animate-spin" />
                      打分赢积分
                    </span>
                  </div>

                  <div className="text-center py-1">
                    <p className="text-xs font-bold text-gray-700 dark:text-gray-300">为骑手阿力的配送服务打个分吧</p>
                    
                    {/* Interactive Stars */}
                    <div className="flex justify-center items-center gap-2.5 my-3">
                      {[1, 2, 3, 4, 5].map((starValue) => {
                        const isFilled = hoveredRating !== null 
                          ? starValue <= hoveredRating 
                          : starValue <= rating;
                        return (
                          <button
                            key={starValue}
                            type="button"
                            onClick={() => handleRatingChange(starValue)}
                            onMouseEnter={() => setHoveredRating(starValue)}
                            onMouseLeave={() => setHoveredRating(null)}
                            className="focus:outline-none transition-transform duration-150 hover:scale-125 cursor-pointer"
                          >
                            <Star
                              size={28}
                              className={`transition-colors duration-200 ${
                                isFilled 
                                  ? 'fill-amber-400 text-amber-400 drop-shadow-sm' 
                                  : 'text-gray-300 dark:text-gray-600'
                              }`}
                            />
                          </button>
                        );
                      })}
                    </div>
                    
                    <p className="text-xs font-semibold text-amber-500 dark:text-amber-400 h-4">
                      {getRatingLabel(hoveredRating !== null ? hoveredRating : rating)}
                    </p>
                  </div>

                  {/* Feedback Tags Selection */}
                  <div className="space-y-2 mt-1">
                    <p className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">勾选服务反馈标签</p>
                    <div className="flex flex-wrap gap-1.5">
                      {(rating >= 4
                        ? ['准时送达', '礼貌热情', '风雨无阻', '包装完好', '快速准时', '餐品完好']
                        : ['配送延误', '服务态度差', '餐品倾洒', '未按备注', '提前点送达', '电话不通']
                      ).map((tag) => {
                        const isSelected = selectedTags.includes(tag);
                        return (
                          <button
                            key={tag}
                            type="button"
                            onClick={() => toggleTag(tag)}
                            className={`px-3 py-1.5 rounded-full text-xs font-medium transition duration-200 cursor-pointer ${
                              isSelected
                                ? 'bg-red-500 text-white shadow-sm border border-transparent'
                                : 'bg-gray-50 hover:bg-gray-100 dark:bg-gray-800 dark:hover:bg-gray-750 text-gray-600 dark:text-gray-300 border border-gray-100 dark:border-gray-800'
                            }`}
                          >
                            {tag}
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Comment input */}
                  <textarea
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    placeholder="写下您对骑手配送的评语（选填）..."
                    maxLength={100}
                    rows={2}
                    className="w-full p-2.5 text-xs rounded-xl bg-gray-50 dark:bg-gray-800 border border-gray-100 dark:border-gray-800 text-gray-800 dark:text-gray-100 focus:outline-none focus:ring-1 focus:ring-red-500/50 resize-none transition"
                  />

                  {/* Submit Button */}
                  <button
                    type="button"
                    onClick={handleSubmitReview}
                    className="w-full py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-bold text-xs shadow-md transition duration-200 cursor-pointer flex items-center justify-center gap-1.5"
                  >
                    <ThumbsUp size={13} />
                    <span>提交评价</span>
                  </button>
                </>
              ) : (
                <>
                  <div className="flex items-center justify-between border-b border-gray-50 dark:border-gray-800/50 pb-2">
                    <span className="font-extrabold text-xs text-gray-400 uppercase tracking-wider">骑手服务评价</span>
                    <span className="text-[10px] text-green-500 font-bold bg-green-500/10 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                      <CheckCircle2 size={10} />
                      已评价
                    </span>
                  </div>

                  <div className="flex flex-col items-center py-2.5 bg-gray-50/50 dark:bg-gray-800/30 rounded-xl">
                    {/* Read only stars */}
                    <div className="flex gap-1">
                      {[1, 2, 3, 4, 5].map((starValue) => (
                        <Star
                          key={starValue}
                          size={18}
                          className={`${
                            starValue <= review.rating 
                              ? 'fill-amber-400 text-amber-400' 
                              : 'text-gray-200 dark:text-gray-700'
                          }`}
                        />
                      ))}
                    </div>
                    <span className="text-xs font-bold text-gray-700 dark:text-gray-300 mt-1.5">
                      {review.rating === 5 && "超级满意，五星好评"}
                      {review.rating === 4 && "非常满意"}
                      {review.rating === 3 && "服务一般"}
                      {review.rating === 2 && "服务不佳"}
                      {review.rating === 1 && "非常差评"}
                    </span>
                  </div>

                  {review.tags && review.tags.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {review.tags.map((tag) => (
                        <span 
                          key={tag}
                          className="px-2.5 py-1 rounded-full text-[10px] font-semibold bg-red-500/10 text-red-500"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  )}

                  {review.comment && (
                    <p className="text-xs text-gray-500 dark:text-gray-400 italic bg-gray-50 dark:bg-gray-800/50 p-2.5 rounded-xl border border-gray-100 dark:border-gray-800/30 leading-relaxed">
                      “ {review.comment} ”
                    </p>
                  )}

                  <p className="text-[10px] text-gray-400 text-center font-medium mt-1">
                    感谢您的支持！您的星级与评价将激励骑手提供更优质的配送。
                  </p>
                </>
              )}
            </div>
          )}

          {/* Order Itemized Receipt summary */}
          <div className="p-3 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm flex flex-col shrink-0">
            <div>
              <div className="flex justify-between items-center pb-2 border-b border-gray-50 dark:border-gray-800/50 mb-2">
                <span className="font-extrabold text-xs text-gray-400 uppercase tracking-wider">配售详情</span>
                <span className="text-xs font-bold text-gray-700 dark:text-gray-300">{order.merchantName}</span>
              </div>

              {/* Items scroll */}
              <div className="space-y-2 max-h-[80px] overflow-y-auto pr-1">
                {order.items.map((cartItem: any, idx: number) => (
                  <div key={idx} className="flex justify-between text-xs">
                    <span className="text-gray-600 dark:text-gray-400">
                      {cartItem.item.name} <strong className="text-[10px] text-gray-400 font-mono">x{cartItem.quantity}</strong>
                    </span>
                    <span className="font-semibold text-gray-700 dark:text-gray-300">¥{(cartItem.item.price * cartItem.quantity).toFixed(2)}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Summary details */}
            <div className="border-t border-gray-50 dark:border-gray-800/60 pt-2 mt-2">
              <div className="flex justify-between text-xs text-gray-500 mb-1">
                <span>配送费</span>
                <span>¥{order.deliveryFee.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center mt-2">
                <span className="text-sm font-bold text-gray-800 dark:text-gray-200">总计实付</span>
                <span className="text-base font-black text-red-500 dark:text-red-400">¥{order.totalAmount.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Complete action */}
          <button
            onClick={onBackToHome}
            className="w-full py-2.5 rounded-2xl bg-red-500 hover:bg-red-600 text-white font-bold text-sm tracking-wide shadow-md cursor-pointer text-center block transition duration-300 shrink-0"
          >
            返回商户首页
          </button>
        </div>
      </div>

      {/* Toast Notification */}
      {showSuccessToast && (
        <motion.div 
          initial={{ opacity: 0, y: 15, x: '-50%' }}
          animate={{ opacity: 1, y: 0, x: '-50%' }}
          className="fixed bottom-6 left-1/2 bg-gray-950/95 dark:bg-white/95 text-white dark:text-gray-900 px-4 py-2.5 rounded-xl shadow-2xl flex items-center gap-2 text-xs font-extrabold z-50 backdrop-blur"
        >
          <Sparkles size={14} className="text-amber-400 animate-pulse" />
          <span>感谢您的评价，评价提交成功！</span>
        </motion.div>
      )}
    </div>
  );
}
