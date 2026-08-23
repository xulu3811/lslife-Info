/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { CreditCard, Check, X, ShieldAlert, Sparkles, CheckCircle2, ChevronRight, Lock } from 'lucide-react';

interface PaymentModalProps {
  isOpen: boolean;
  orderId: string;
  amount: number;
  merchantName: string;
  isDarkMode?: boolean;
  onClose: () => void;
  onPaymentSuccess: (method: 'alipay' | 'wechat' | 'wallet') => void;
}

export default function PaymentModal({
  isOpen,
  orderId,
  amount,
  merchantName,
  isDarkMode = false,
  onClose,
  onPaymentSuccess,
}: PaymentModalProps) {
  const [selectedMethod, setSelectedMethod] = useState<'alipay' | 'wechat' | 'wallet'>('alipay');
  const [paymentState, setPaymentState] = useState<'idle' | 'processing' | 'success' | 'failed'>('idle');
  const [countdown, setCountdown] = useState<number>(3);

  // Auto-redirect or countdown on successful payment
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (paymentState === 'success' && countdown > 0) {
      timer = setTimeout(() => {
        setCountdown(prev => prev - 1);
      }, 1000);
    } else if (paymentState === 'success' && countdown === 0) {
      onPaymentSuccess(selectedMethod);
    }
    return () => clearTimeout(timer);
  }, [paymentState, countdown]);

  const handlePay = () => {
    setPaymentState('processing');
    // Simulate high-fidelity smooth verification/authorization with sound effects or beautiful screen transition
    setTimeout(() => {
      setPaymentState('success');
      // Conceptually trigger voice synthesis or system vibration (via WebAudio API synthesized coin ring)
      try {
        const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
        // Play classic "ka-ching" double coin bell tone
        const playTone = (freq: number, start: number, duration: number) => {
          const osc = audioCtx.createOscillator();
          const gainNode = audioCtx.createGain();
          osc.connect(gainNode);
          gainNode.connect(audioCtx.destination);
          
          osc.type = 'sine';
          osc.frequency.setValueAtTime(freq, start);
          gainNode.gain.setValueAtTime(0.3, start);
          gainNode.gain.exponentialRampToValueAtTime(0.001, start + duration);
          
          osc.start(start);
          osc.stop(start + duration);
        };
        const now = audioCtx.currentTime;
        playTone(987.77, now, 0.12); // B5
        playTone(1318.51, now + 0.08, 0.4); // E6
      } catch (e) {
        console.log('AudioContext blocked or unsupported');
      }
    }, 1800);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Dark overlay backdrop */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
        onClick={() => paymentState === 'idle' && onClose()}
      />

      {/* Payment Sheet Container */}
      <div
        className={`relative w-full max-w-md overflow-hidden rounded-2xl shadow-2xl border transition-colors duration-300 ${
          isDarkMode ? 'bg-gray-900 border-gray-800 text-white' : 'bg-white border-gray-100 text-gray-900'
        }`}
      >
        <AnimatePresence mode="wait">
          {paymentState === 'idle' && (
            <motion.div
              key="idle"
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -15 }}
              className="p-4"
            >
              {/* Header */}
              <div className="flex items-center justify-between pb-4 border-b border-gray-100 dark:border-gray-800">
                <div className="flex items-center gap-2">
                  <Lock size={16} className="text-red-500 animate-pulse" />
                  <span className="font-bold text-sm tracking-wide">安全支付收银台</span>
                </div>
                <button 
                  onClick={onClose}
                  className="p-1 rounded-full text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition"
                >
                  <X size={18} />
                </button>
              </div>

              {/* Amount Display */}
              <div className="text-center py-4">
                <p className="text-xs text-gray-400 dark:text-gray-400 font-medium">订单金额</p>
                <div className="flex items-baseline justify-center gap-1 mt-1 text-red-500 dark:text-red-400">
                  <span className="text-[17px] font-bold">¥</span>
                  <span className="text-4xl font-extrabold tracking-tight">{amount.toFixed(2)}</span>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-2 truncate max-w-xs mx-auto">
                  收款方: {merchantName}
                </p>
                <p className="text-[10px] text-gray-400 font-mono mt-1">
                  订单号: {orderId}
                </p>
              </div>

              {/* Payment Method Select */}
              <div className="space-y-3 my-2">
                <p className="text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">选择支付方式</p>

                {/* Alipay */}
                <button
                  onClick={() => setSelectedMethod('alipay')}
                  className={`w-full flex items-center justify-between p-4 rounded-xl border text-left transition ${
                    selectedMethod === 'alipay' 
                      ? 'border-red-500 bg-red-500/5 dark:bg-red-500/10' 
                      : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-red-500 flex items-center justify-center text-white font-black text-lg">
                      支
                    </div>
                    <div>
                      <p className="font-bold text-sm">支付宝支付 (Alipay)</p>
                      <p className="text-xs text-gray-400 dark:text-gray-400 mt-0.5">支持花呗、信用卡、免密极速付</p>
                    </div>
                  </div>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all ${
                    selectedMethod === 'alipay' ? 'border-red-500 bg-red-500' : 'border-gray-300 dark:border-gray-600'
                  }`}>
                    {selectedMethod === 'alipay' && <Check size={12} className="text-white" />}
                  </div>
                </button>

                {/* WeChat Pay */}
                <button
                  onClick={() => setSelectedMethod('wechat')}
                  className={`w-full flex items-center justify-between p-4 rounded-xl border text-left transition ${
                    selectedMethod === 'wechat' 
                      ? 'border-green-500 bg-green-500/5 dark:bg-green-500/10' 
                      : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-green-500 flex items-center justify-center text-white font-black text-lg">
                      微
                    </div>
                    <div>
                      <p className="font-bold text-sm">微信支付 (WeChat Pay)</p>
                      <p className="text-xs text-gray-400 dark:text-gray-400 mt-0.5">支持零钱、理财通、极速支付</p>
                    </div>
                  </div>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all ${
                    selectedMethod === 'wechat' ? 'border-green-500 bg-green-500' : 'border-gray-300 dark:border-gray-600'
                  }`}>
                    {selectedMethod === 'wechat' && <Check size={12} className="text-white" />}
                  </div>
                </button>

                {/* Wallet Balance */}
                <button
                  onClick={() => setSelectedMethod('wallet')}
                  className={`w-full flex items-center justify-between p-4 rounded-xl border text-left transition ${
                    selectedMethod === 'wallet' 
                      ? 'border-amber-500 bg-amber-500/5 dark:bg-amber-500/10' 
                      : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-amber-500 flex items-center justify-center text-white">
                      <CreditCard size={20} />
                    </div>
                    <div>
                      <p className="font-bold text-sm">连山同城零钱 (余额)</p>
                      <p className="text-xs text-amber-500 dark:text-amber-400 mt-0.5 font-semibold">余额: ¥245.80 (立减0.5元)</p>
                    </div>
                  </div>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all ${
                    selectedMethod === 'wallet' ? 'border-amber-500 bg-amber-500' : 'border-gray-300 dark:border-gray-600'
                  }`}>
                    {selectedMethod === 'wallet' && <Check size={12} className="text-white" />}
                  </div>
                </button>
              </div>

              {/* Secure Info */}
              <div className="flex items-center justify-center gap-1.5 py-2 text-[11px] text-gray-400">
                <CheckCircle2 size={12} className="text-green-500" />
                <span>银联/微信/支付宝联合风控，保障交易安全</span>
              </div>

              {/* Action Button */}
              <button
                onClick={handlePay}
                className={`w-full mt-4 py-3.5 rounded-xl font-bold text-white text-base transition duration-300 flex items-center justify-center gap-2 cursor-pointer shadow-lg ${
                  selectedMethod === 'alipay' ? 'bg-red-500 shadow-blue-500/20 hover:bg-red-600' :
                  selectedMethod === 'wechat' ? 'bg-green-500 shadow-green-500/20 hover:bg-green-600' :
                  'bg-amber-500 shadow-amber-500/20 hover:bg-amber-600'
                }`}
              >
                <span>确认支付 ¥{amount.toFixed(2)}</span>
              </button>
            </motion.div>
          )}

          {paymentState === 'processing' && (
            <motion.div
              key="processing"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="p-8 text-center flex flex-col items-center justify-center min-h-[350px]"
            >
              {/* Spinner */}
              <div className="relative w-20 h-20 mb-6">
                <div className={`absolute inset-0 rounded-full border-4 opacity-10 ${
                  selectedMethod === 'alipay' ? 'border-red-500' :
                  selectedMethod === 'wechat' ? 'border-green-500' : 'border-amber-500'
                }`} />
                <div className={`absolute inset-0 rounded-full border-4 border-t-transparent animate-spin ${
                  selectedMethod === 'alipay' ? 'border-red-500' :
                  selectedMethod === 'wechat' ? 'border-green-500' : 'border-amber-500'
                }`} />
              </div>
              
              <h3 className="text-lg font-bold">正在安全支付中</h3>
              <p className="text-xs text-gray-400 dark:text-gray-400 mt-2 max-w-xs leading-relaxed">
                正在请求连山同城加密收银后台接口，请勿关闭页面或断开网络连接...
              </p>
            </motion.div>
          )}

          {paymentState === 'success' && (
            <motion.div
              key="success"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="p-8 text-center flex flex-col items-center justify-center min-h-[380px]"
            >
              {/* Animated Success ring */}
              <div className="w-16 h-16 rounded-full bg-green-500/10 flex items-center justify-center text-green-500 mb-6 scale-110">
                <CheckCircle2 size={44} className="stroke-[2.5px]" />
              </div>

              <span className="bg-green-500/10 text-green-600 dark:text-green-400 text-[10px] font-bold px-3 py-1 rounded-full uppercase tracking-wider mb-2">
                支付已安全授权
              </span>
              <h3 className="text-[17px] font-extrabold tracking-tight">支付成功!</h3>
              
              <div className="mt-4 p-4 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-100 dark:border-gray-800/60 w-full">
                <div className="flex justify-between text-xs py-1">
                  <span className="text-gray-400">支付方式</span>
                  <span className="font-semibold text-gray-700 dark:text-gray-300">
                    {selectedMethod === 'alipay' ? '支付宝极速付' :
                     selectedMethod === 'wechat' ? '微信零钱支付' : '连山同城零钱'}
                  </span>
                </div>
                <div className="flex justify-between text-xs py-1">
                  <span className="text-gray-400">实付金额</span>
                  <span className="font-extrabold text-red-500 dark:text-red-400">¥{amount.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-xs py-1">
                  <span className="text-gray-400">商户收款</span>
                  <span className="font-semibold text-gray-700 dark:text-gray-300 truncate max-w-[150px]">{merchantName}</span>
                </div>
              </div>

              <div className="text-xs text-gray-400 dark:text-gray-400 mt-6 flex items-center justify-center gap-1.5">
                <Sparkles size={14} className="text-amber-500 animate-spin-slow" />
                <span>将在 <strong className="text-red-500 font-bold">{countdown}</strong> 秒后自动跳转至订单...</span>
              </div>

              <button
                onClick={() => onPaymentSuccess(selectedMethod)}
                className="w-full mt-5 py-3 rounded-xl bg-green-500 text-white font-bold text-sm hover:bg-green-600 transition shadow-lg shadow-green-500/15 cursor-pointer flex items-center justify-center gap-1"
              >
                <span>立即查看订单状态</span>
                <ChevronRight size={16} />
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
