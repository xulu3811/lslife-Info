import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, CheckCircle2, Zap, Crown, Star } from 'lucide-react';

interface SubscriptionModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentTier: 'free' | 'vip' | 'premium';
  onUpgrade: (tier: 'vip' | 'premium') => void;
}

export default function SubscriptionModal({ isOpen, onClose, currentTier, onUpgrade }: SubscriptionModalProps) {
  const [selectedPlan, setSelectedPlan] = useState<'vip' | 'premium'>('vip');

  const plans = [
    {
      id: 'vip',
      name: '普通会员',
      price: '9.9',
      limit: 20,
      icon: Star,
      color: 'from-red-500 to-indigo-600',
      textColor: 'text-red-500',
      bgColor: 'bg-red-50 dark:bg-red-500/10',
      borderColor: 'border-red-200 dark:border-red-800',
      activeBorder: 'border-red-500',
      features: ['每月可发布20条信息', '优先展示特权', '专属会员标识']
    },
    {
      id: 'premium',
      name: '高级会员',
      price: '19.9',
      limit: 50,
      icon: Crown,
      color: 'from-amber-400 to-orange-500',
      textColor: 'text-amber-500',
      bgColor: 'bg-amber-50 dark:bg-amber-500/10',
      borderColor: 'border-amber-200 dark:border-amber-800',
      activeBorder: 'border-amber-500',
      features: ['每月可发布50条信息', '全站置顶特权', '尊贵皇冠标识', '专属客服通道']
    }
  ];

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
          className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        />
        <motion.div 
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="relative w-full max-w-sm bg-white dark:bg-gray-900 rounded-3xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh]"
        >
          <div className="p-4 pb-4 border-b border-gray-100 dark:border-gray-800 flex items-center justify-between sticky top-0 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md z-10">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white">
                <Zap size={16} fill="currentColor" />
              </div>
              <h3 className="font-black text-gray-900 dark:text-white text-lg">升级会员权限</h3>
            </div>
            <button 
              onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 transition"
            >
              <X size={18} strokeWidth={2.5} />
            </button>
          </div>

          <div className="overflow-y-auto p-4 pb-4">
            <div className="text-center mb-6">
              <p className="text-[13px] text-gray-500 dark:text-gray-400">
                普通用户每月仅可发布 10 条信息，升级会员解锁更多发布额度和专属特权
              </p>
            </div>

            <div className="space-y-4">
              {plans.map((plan) => {
                const Icon = plan.icon;
                const isSelected = selectedPlan === plan.id;
                const isCurrent = currentTier === plan.id;
                
                return (
                  <div 
                    key={plan.id}
                    onClick={() => !isCurrent && setSelectedPlan(plan.id as 'vip' | 'premium')}
                    className={`relative p-4 rounded-xl border-2 transition-all cursor-pointer ${
                      isSelected 
                        ? plan.activeBorder + ' shadow-md' 
                        : plan.borderColor + ' opacity-70 hover:opacity-100'
                    } ${isCurrent ? 'opacity-50 cursor-not-allowed grayscale' : ''}`}
                  >
                    {isCurrent && (
                      <div className="absolute top-0 right-0 bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300 text-[10px] font-bold px-2 py-1 rounded-bl-xl rounded-tr-xl">
                        当前生效
                      </div>
                    )}
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-xl ${plan.bgColor} flex items-center justify-center ${plan.textColor}`}>
                          <Icon size={20} strokeWidth={2.5} />
                        </div>
                        <div>
                          <h4 className="font-black text-gray-900 dark:text-white text-lg">{plan.name}</h4>
                          <div className="flex items-center gap-1 mt-0.5">
                            <span className="text-[11px] font-medium text-gray-500">每月可发</span>
                            <span className={`text-[12px] font-bold ${plan.textColor}`}>{plan.limit}条</span>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className={`text-lg font-black ${plan.textColor}`}>
                          <span className="text-[13px] mr-0.5">¥</span>{plan.price}
                          <span className="text-[11px] font-medium text-gray-500 ml-0.5">/月</span>
                        </div>
                      </div>
                    </div>
                    
                    <ul className="space-y-2 mt-4 pt-4 border-t border-gray-100 dark:border-gray-800">
                      {plan.features.map((feature, idx) => (
                        <li key={idx} className="flex items-center gap-2 text-[13px] text-gray-600 dark:text-gray-300 font-medium">
                          <CheckCircle2 size={14} className={plan.textColor} />
                          {feature}
                        </li>
                      ))}
                    </ul>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="p-4 border-t border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-900/50">
            <button 
              onClick={() => {
                onUpgrade(selectedPlan);
                onClose();
              }}
              className={`w-full py-4 rounded-2xl text-white font-black text-[16px] transition active:scale-95 flex items-center justify-center gap-2 bg-gradient-to-r ${plans.find(p => p.id === selectedPlan)?.color} shadow-lg`}
            >
              立即开通
            </button>
            <p className="text-center text-[10px] text-gray-400 mt-3">
              开通即视为同意《会员服务协议》
            </p>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
