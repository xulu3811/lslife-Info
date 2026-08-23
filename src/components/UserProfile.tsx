/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useMemo, useState, useEffect } from 'react';
import { useLanguage } from '../contexts/LanguageContext';

import { motion, AnimatePresence } from 'motion/react';
import { 
  ArrowLeft, Settings, ShieldCheck, Wallet, Ticket, Award, BellRing, Bell, 
  CheckCheck, Trash2, ChevronDown, Info, Phone, ShoppingBag, ChevronRight, 
  Heart, MapPin, Plus, Check, Edit, X, User, Globe, FileText, LogOut
} from 'lucide-react';


interface AppNotification {
  id: string;
  type: 'merchant_accept' | 'preparing' | 'delivering' | 'delivered' | 'system';
  title: string;
  content: string;
  riderName?: string;
  riderPhone?: string;
  timestamp: string;
  read: boolean;
  orderId?: string;
}

const SEED_NOTIFICATIONS: AppNotification[] = [
  {
    id: 'sys-1',
    type: 'system',
    title: '🎉 实名认证成功 · 奖励已到账',
    content: '您的个人实名认证已绑定，连山同城为您解锁 “金牌吃货” 尊贵特权！',
    timestamp: '昨天 18:30',
    read: true,
  },
  {
    id: 'sys-2',
    type: 'system',
    title: '🎫 同城免配送费体验券',
    content: '特派团队送您 2 张同城商家免配送费体验券，点击下方“我的权益”可查收！',
    timestamp: '前天 10:15',
    read: true,
  }
];

export interface DeliveryAddress {
  id: string;
  name: string;
  phone: string;
  tag: '家' | '公司' | '学校' | '其他';
  address: string;
  isDefault: boolean;
}

const SEED_ADDRESSES: DeliveryAddress[] = [
  {
    id: 'addr-1',
    name: '莫小美',
    phone: '13812345678',
    tag: '家',
    address: '广东省清远市连山壮族瑶族自治县吉田镇吉祥路88号3楼',
    isDefault: true
  },
  {
    id: 'addr-2',
    name: '雷大哥',
    phone: '13987654321',
    tag: '公司',
    address: '广东省清远市连山壮族瑶族自治县壮乡风情街瑶族特色农家乐',
    isDefault: false
  }
];

interface UserProfileProps {
  isDarkMode?: boolean;
  onBackToHome: () => void;
  ordersCount: number;
  currentOrderAmount: number;
  activeOrderId?: string | null;
  membershipTier?: 'free' | 'vip' | 'premium';
  onShowSubscription?: () => void;
}

export default function UserProfile({
  isDarkMode = false,
  onBackToHome,
  ordersCount = 0,
  currentOrderAmount = 0,
  activeOrderId = null,
  membershipTier = 'free',
  onShowSubscription
}: UserProfileProps) {
  const { language, setLanguage, t } = useLanguage();
  
  // Real-time notification states
  const [notifications, setNotifications] = useState<AppNotification[]>(() => {
    const saved = localStorage.getItem('user_notifications');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        return SEED_NOTIFICATIONS;
      }
    }
    return SEED_NOTIFICATIONS;
  });

  const [isNotificationsExpanded, setIsNotificationsExpanded] = useState<boolean>(true);

  // Sync to local storage
  useEffect(() => {
    localStorage.setItem('user_notifications', JSON.stringify(notifications));
  }, [notifications]);

  // Poll order status changes and insert real-time notifications
  useEffect(() => {
    if (!activeOrderId) return;

    let isMounted = true;
    
    const checkOrderStatus = async () => {
      try {
        const res = await fetch(`/api/orders/${activeOrderId}`);
        if (!res.ok) return;
        const order = await res.json();
        
        if (!isMounted) return;

        setNotifications((prev) => {
          const updated = [...prev];
          let changed = false;

          // 1. Merchant accepted / preparing (always present once order exists)
          const hasPreparing = updated.some(n => n.orderId === activeOrderId && n.type === 'preparing');
          if (!hasPreparing) {
            // Push merchant accept alert
            updated.unshift({
              id: `${activeOrderId}-accept`,
              type: 'merchant_accept',
              title: '🏮 商家接单：瑶家美味制作中',
              content: `【${order.merchantName}】已接单并开始为您烹饪，精选连山非遗食材，火候正佳！`,
              timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
              read: false,
              orderId: activeOrderId
            });
            // Push rider ready notice
            updated.unshift({
              id: `${activeOrderId}-preparing`,
              type: 'preparing',
              title: '🛵 骑手接单：小哥正赶往商户',
              content: `同城先锋骑手【${order.rider.name}】已成功接单。小哥联系方式：${order.rider.phone}，正骑车赶往商家取货。`,
              timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
              read: false,
              orderId: activeOrderId,
              riderName: order.rider.name,
              riderPhone: order.rider.phone
            });
            changed = true;
          }

          // 2. Rider delivering state
          if (order.status === 'delivering' || order.status === 'delivered') {
            const hasDelivering = updated.some(n => n.orderId === activeOrderId && n.type === 'delivering');
            if (!hasDelivering) {
              updated.unshift({
                id: `${activeOrderId}-delivering`,
                type: 'delivering',
                title: '⚡ 配送进行中：骑手风雨无阻',
                content: `骑手【${order.rider.name}】已取到您的热乎宝贝，正在全速向您配送中！联系方式：${order.rider.phone}。`,
                timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
                read: false,
                orderId: activeOrderId,
                riderName: order.rider.name,
                riderPhone: order.rider.phone
              });
              changed = true;
            }
          }

          // 3. Delivered state
          if (order.status === 'delivered') {
            const hasDelivered = updated.some(n => n.orderId === activeOrderId && n.type === 'delivered');
            if (!hasDelivered) {
              updated.unshift({
                id: `${activeOrderId}-delivered`,
                type: 'delivered',
                title: '✅ 妥投通知：您的美食已安全送达',
                content: `骑手【${order.rider.name}】已将美味安全送达！祝您用餐愉快。觉得满意请给他一个超值五星好评吧！`,
                timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
                read: false,
                orderId: activeOrderId,
                riderName: order.rider.name,
                riderPhone: order.rider.phone
              });
              changed = true;
            }
          }

          return changed ? updated : prev;
        });
      } catch (err) {
        console.error('Polling error in UserProfile notifications:', err);
      }
    };

    // Run check immediately
    checkOrderStatus();

    // Check status every 4 seconds
    const intervalId = setInterval(checkOrderStatus, 4000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [activeOrderId]);

  const unreadCount = useMemo(() => {
    return notifications.filter(n => !n.read).length;
  }, [notifications]);

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const clearNotifications = () => {
    setNotifications([]);
  };

  const handleNotificationClick = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  // Delivery address states & handlers
  const [addresses, setAddresses] = useState<DeliveryAddress[]>(() => {
    const saved = localStorage.getItem('user_delivery_addresses');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        return SEED_ADDRESSES;
      }
    }
    return SEED_ADDRESSES;
  });

  // Sync addresses to localStorage
  useEffect(() => {
    localStorage.setItem('user_delivery_addresses', JSON.stringify(addresses));
  }, [addresses]);

  const [isAddressSectionOpen, setIsAddressSectionOpen] = useState<boolean>(false);
  const [isAddressModalOpen, setIsAddressModalOpen] = useState<boolean>(false);
  const [editingAddress, setEditingAddress] = useState<DeliveryAddress | null>(null);

  // Form states
  const [formName, setFormName] = useState<string>('');
  const [formPhone, setFormPhone] = useState<string>('');
  const [formTag, setFormTag] = useState<'家' | '公司' | '学校' | '其他'>('家');
  const [formAddressStr, setFormAddressStr] = useState<string>('');
  const [formIsDefault, setFormIsDefault] = useState<boolean>(false);
  const [formError, setFormError] = useState<string>('');

  const handleOpenAddAddress = () => {
    setEditingAddress(null);
    setFormName('');
    setFormPhone('');
    setFormTag('家');
    setFormAddressStr('');
    setFormIsDefault(addresses.length === 0);
    setFormError('');
    setIsAddressModalOpen(true);
  };

  const handleOpenEditAddress = (addr: DeliveryAddress, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingAddress(addr);
    setFormName(addr.name);
    setFormPhone(addr.phone);
    setFormTag(addr.tag);
    setFormAddressStr(addr.address);
    setFormIsDefault(addr.isDefault);
    setFormError('');
    setIsAddressModalOpen(true);
  };

  const handleDeleteAddress = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const updated = addresses.filter(a => a.id !== id);
    const wasDefault = addresses.find(a => a.id === id)?.isDefault;
    if (wasDefault && updated.length > 0) {
      updated[0].isDefault = true;
    }
    setAddresses(updated);
  };

  const handleSetDefaultAddress = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const updated = addresses.map(a => ({
      ...a,
      isDefault: a.id === id
    }));
    setAddresses(updated);
  };

  const handleSaveAddress = () => {
    if (!formName.trim()) {
      setFormError('请输入收货人姓名');
      return;
    }
    if (!formPhone.trim()) {
      setFormError('请输入联系电话');
      return;
    }
    if (!/^1\d{10}$/.test(formPhone.trim())) {
      setFormError('请输入正确的11位手机号码');
      return;
    }
    if (!formAddressStr.trim()) {
      setFormError('请输入详细收货地址');
      return;
    }

    let updated: DeliveryAddress[];

    if (editingAddress) {
      updated = addresses.map(a => {
        if (a.id === editingAddress.id) {
          return {
            ...a,
            name: formName.trim(),
            phone: formPhone.trim(),
            tag: formTag,
            address: formAddressStr.trim(),
            isDefault: formIsDefault
          };
        }
        return a;
      });
    } else {
      const newAddress: DeliveryAddress = {
        id: 'addr-' + Date.now(),
        name: formName.trim(),
        phone: formPhone.trim(),
        tag: formTag,
        address: formAddressStr.trim(),
        isDefault: formIsDefault
      };
      updated = [newAddress, ...addresses];
    }

    if (formIsDefault) {
      const targetId = editingAddress ? editingAddress.id : updated[0].id;
      updated = updated.map(a => ({
        ...a,
        isDefault: a.id === targetId
      }));
    } else {
      const hasDefault = updated.some(a => a.isDefault);
      if (!hasDefault && updated.length > 0) {
        updated[0].isDefault = true;
      }
    }

    setAddresses(updated);
    setIsAddressModalOpen(false);
  };


  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [activeSettingView, setActiveSettingView] = useState<'main' | 'profile' | 'notifications' | 'privacy' | 'locations' | 'terms' | 'policy'>('main');


  return (
    <div className={`relative flex flex-col h-full overflow-hidden transition-colors duration-300 ${
      isDarkMode ? 'bg-black text-gray-100' : 'bg-gray-50 text-gray-900'
    }`}>
      {/* Scrollable Container */}
      <div className="flex-1 overflow-y-auto space-y-4">
      {/* Top Profile Card Header */}
      <div className="p-3 bg-gradient-to-br from-red-500 to-indigo-600 text-white shrink-0 shadow-md">
        <div className="flex items-center justify-between">
          <button
            onClick={onBackToHome}
            className="p-1.5 rounded-full hover:bg-white/10 transition cursor-pointer"
          >
            <ArrowLeft size={22} />
          </button>
          <h2 className="text-sm font-black tracking-wide">个人中心</h2>
          <button 
            onClick={() => setShowSettings(true)}
            className="p-1.5 rounded-full hover:bg-white/10 transition cursor-pointer"
          >
            <Settings size={22} />
          </button>
        </div>

        {/* User Badge Info */}
        <div className="flex items-center gap-4 mt-5 pb-2">
          <div className="relative w-16 h-16 rounded-full border-2 border-white/40 overflow-hidden bg-white/20 flex items-center justify-center">
            <img
              src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80"
              alt="User Avatar"
              referrerPolicy="no-referrer"
              className="w-full h-full object-cover"
            />
            {membershipTier === 'premium' && (
              <div className="absolute -bottom-1 -right-1 bg-amber-400 p-1 rounded-full text-[8px] border border-red-500 text-gray-950 font-black">
                👑
              </div>
            )}
            {membershipTier === 'vip' && (
              <div className="absolute -bottom-1 -right-1 bg-red-400 p-1 rounded-full text-[8px] border border-red-500 text-white font-black">
                ✨
              </div>
            )}
          </div>
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h3 className="text-base font-black tracking-tight">{t('profile.username')} (LianShan_A9)</h3>
              {membershipTier === 'premium' && (
                <span className="text-[9px] font-bold bg-amber-400 text-gray-900 px-1.5 py-0.5 rounded-md flex items-center gap-0.5">
                  {t('profile.member_premium')}
                </span>
            )}
              {membershipTier === 'vip' && (
                <span className="text-[9px] font-bold bg-red-100 text-red-800 px-1.5 py-0.5 rounded-md flex items-center gap-0.5">
                  {t('profile.member_vip')}
                </span>
            )}
              {membershipTier === 'free' && (
                <span className="text-[9px] font-bold bg-gray-200 text-gray-600 px-1.5 py-0.5 rounded-md flex items-center gap-0.5 cursor-pointer" onClick={onShowSubscription}>
                  {t('profile.member_free')}
                </span>
            )}
            </div>
            <p className="text-[10px] text-red-100 font-medium opacity-90 flex items-center gap-1">
              <ShieldCheck size={11} className="text-amber-300" />
              <span>{t('profile.verified')}</span>
            </p>
          </div>
        </div>
      </div>

      {/* Asset grid summary */}
      <div className="px-4 -mt-4 shrink-0">
        <div className="grid grid-cols-3 gap-3 bg-white dark:bg-gray-900 p-4 rounded-3xl border border-gray-100 dark:border-gray-800 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)]">
          
          {/* Wallet Balance */}
          <div className="text-center space-y-1.5 flex flex-col items-center">
            <div className="w-8 h-8 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center text-red-500 mb-1">
              <Wallet size={20} strokeWidth={2.5} />
            </div>
            <p className="text-[10px] text-gray-500 dark:text-gray-400 font-bold">
              <span>{t('profile.balance')}</span>
            </p>
            <h4 className="text-[13px] font-black text-gray-900 dark:text-white">
              ¥248.5
            </h4>
          </div>

          {/* Coupons */}
          <div className="text-center space-y-1.5 flex flex-col items-center border-x border-gray-50 dark:border-gray-800">
            <div className="w-8 h-8 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center text-red-500 mb-1">
              <Ticket size={20} strokeWidth={2.5} />
            </div>
            <p className="text-[10px] text-gray-500 dark:text-gray-400 font-bold">
              <span>{t('profile.coupons')}</span>
            </p>
            <h4 className="text-[13px] font-black text-red-500">
              3 {t('profile.coupon_unit')}
            </h4>
          </div>

          {/* Points */}
          <div className="text-center space-y-1.5 flex flex-col items-center">
            <div className="w-8 h-8 rounded-full bg-amber-50 dark:bg-amber-500/10 flex items-center justify-center text-amber-500 mb-1">
              <Award size={20} strokeWidth={2.5} />
            </div>
            <p className="text-[10px] text-gray-500 dark:text-gray-400 font-bold">
              <span>{t('profile.points')}</span>
            </p>
            <h4 className="text-[13px] font-black text-amber-500">
              1,250 pt
            </h4>
          </div>
        </div>
      </div>

      <div className="p-4 space-y-4">
        {/* Member Privileges benefits section */}
        <div className="p-3 rounded-[14px] bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm space-y-3">
          <p className="text-xs font-extrabold text-gray-400 uppercase tracking-wider pb-1.5 border-b border-gray-50 dark:border-gray-800/40">
            {t('profile.my_benefits')}
          </p>
          <div className="grid grid-cols-2 gap-3">
            <div className="p-2.5 rounded-xl bg-amber-500/5 border border-amber-500/10 flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500 text-sm font-black">
                🍗
              </div>
              <div>
                <h5 className="text-[11px] font-bold">{t('profile.benefit_1_title')}</h5>
                <p className="text-[9px] text-gray-400 mt-0.5">{t('profile.benefit_1_desc')}</p>
              </div>
            </div>
            <div className="p-2.5 rounded-xl bg-red-500/5 border border-red-500/10 flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-red-500/10 flex items-center justify-center text-red-500 text-sm font-black">
                🛵
              </div>
              <div>
                <h5 className="text-[11px] font-bold">{t('profile.benefit_2_title')}</h5>
                <p className="text-[9px] text-gray-400 mt-0.5">{t('profile.benefit_2_desc')}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Real-time Message Notifications section */}
        <div className="p-3 rounded-[14px] bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <button 
              onClick={() => setIsNotificationsExpanded(!isNotificationsExpanded)}
              className="flex items-center gap-2 text-left cursor-pointer focus:outline-none"
            >
              <div className="relative">
                {unreadCount > 0 ? (
                  <BellRing size={15} className="text-red-500 animate-bounce" />
                ) : (
                  <Bell size={15} className="text-gray-400" />
                )}
                {unreadCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 flex h-3 w-3">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
                  </span>
                )}
              </div>
              <div className="flex items-center gap-1.5">
                <span className="text-xs font-black text-gray-900 dark:text-white">{t('profile.notifications')}</span>
                {unreadCount > 0 && (
                  <span className="px-1.5 py-0.5 text-[9px] font-black bg-red-500 text-white rounded-full min-w-[16px] text-center shadow">
                    {unreadCount}
                  </span>
                )}
              </div>
            </button>
            <div className="flex items-center gap-2">
              {notifications.length > 0 && (
                <>
                  <button 
                    onClick={markAllAsRead}
                    className="text-[10px] text-gray-400 hover:text-red-500 font-extrabold transition flex items-center gap-0.5 px-1.5 py-1 rounded hover:bg-gray-50 dark:hover:bg-gray-800"
                    title="Read All"
                  >
                    <CheckCheck size={11} />
                    <span>Read All</span>
                  </button>
                  <button 
                    onClick={clearNotifications}
                    className="text-[10px] text-gray-400 hover:text-red-500 font-extrabold transition flex items-center gap-0.5 px-1.5 py-1 rounded hover:bg-gray-50 dark:hover:bg-gray-800"
                    title={t('profile.notifications_clear')}
                  >
                    <Trash2 size={11} />
                    <span>{t('profile.notifications_clear')}</span>
                  </button>
                </>
              )}
              <ChevronDown 
                size={14} 
                className={`text-gray-400 transition-transform duration-300 ${isNotificationsExpanded ? 'rotate-180' : ''}`} 
              />
            </div>
          </div>

          <AnimatePresence>
            {isNotificationsExpanded && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: 'auto', opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="overflow-hidden"
              >
                {notifications.length === 0 ? (
                  <div className="text-center py-6 flex flex-col items-center justify-center text-gray-400 dark:text-gray-500">
                    <Info size={24} className="opacity-30 mb-1.5" />
                    <p className="text-[10px] font-bold">{t('profile.notifications_empty')}</p>
                  </div>
                ) : (
                  <div className="space-y-2 mt-2">
                    {notifications.map((n) => (
                      <div 
                        key={n.id}
                        onClick={() => handleNotificationClick(n.id)}
                        className={`relative p-3 rounded-xl border transition-all duration-300 cursor-pointer ${
                          !n.read 
                            ? 'bg-red-50/20 dark:bg-red-950/10 border-red-100/40 dark:border-red-900/30 shadow-sm' 
                            : 'bg-gray-50/50 dark:bg-gray-800/20 border-gray-100/50 dark:border-gray-800/30'
                        } ${
                          n.type === 'merchant_accept' ? 'border-l-[3px] border-l-amber-500' :
                          n.type === 'preparing' ? 'border-l-[3px] border-l-blue-500' :
                          n.type === 'delivering' ? 'border-l-[3px] border-l-cyan-500' :
                          n.type === 'delivered' ? 'border-l-[3px] border-l-green-500' : 'border-l-[3px] border-l-indigo-500'
                        }`}
                      >
                        {!n.read && (
                          <span className="absolute top-3 right-3 h-2 w-2 rounded-full bg-red-500"></span>
                        )}
                        <div className="flex items-center justify-between gap-2">
                          <span className="text-[10px] font-black text-gray-900 dark:text-white flex items-center gap-1">
                            {n.type === 'merchant_accept' && <span className="text-amber-500 bg-amber-500/10 px-1 py-0.5 rounded text-[8px] font-bold whitespace-nowrap">商户接单</span>}
                            {n.type === 'preparing' && <span className="text-red-500 bg-red-500/10 px-1 py-0.5 rounded text-[8px] font-bold whitespace-nowrap">骑手就绪</span>}
                            {n.type === 'delivering' && <span className="text-cyan-500 bg-cyan-500/10 px-1 py-0.5 rounded text-[8px] font-bold whitespace-nowrap">极速配送</span>}
                            {n.type === 'delivered' && <span className="text-green-500 bg-green-500/10 px-1 py-0.5 rounded text-[8px] font-bold whitespace-nowrap">安全送达</span>}
                            {n.type === 'system' && <span className="text-indigo-500 bg-indigo-500/10 px-1 py-0.5 rounded text-[8px] font-bold whitespace-nowrap">系统通知</span>}
                            <span className="truncate max-w-[170px]">{n.title}</span>
                          </span>
                          <span className="text-[9px] text-gray-400 font-medium whitespace-nowrap">{n.timestamp}</span>
                        </div>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400 mt-1.5 leading-relaxed font-semibold">
                          {n.content}
                        </p>
                        {/* Rider Contact details option to call */}
                        {n.riderPhone && (
                          <div className="mt-2.5 pt-2 flex items-center justify-between border-t border-dashed border-gray-100 dark:border-gray-800/60 text-[9px]">
                            <span className="text-gray-400 font-bold">配送员：{n.riderName || '阿力 (特派员)'}</span>
                            <a 
                              href={`tel:${n.riderPhone}`}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleNotificationClick(n.id);
                              }}
                              className="px-2 py-0.5 rounded-md bg-red-500 hover:bg-red-600 text-white font-extrabold flex items-center gap-1 cursor-pointer transition shadow-sm active:scale-95"
                            >
                              <Phone size={9} />
                              <span>呼叫小哥 ({n.riderPhone})</span>
                            </a>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        </div>
        {/* Active Session & Actions list */}
        <div className="rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 overflow-hidden shadow-sm divide-y divide-gray-50 dark:divide-gray-800">
          
          <div className="flex items-center justify-between p-4 text-xs font-bold hover:bg-gray-50 dark:hover:bg-gray-950/40 cursor-pointer transition-colors duration-200">
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-indigo-50 dark:bg-indigo-500/10 flex items-center justify-center">
                <ShoppingBag size={14} className="text-indigo-500" strokeWidth={2.5} />
              </div>
              <span className="text-gray-800 dark:text-gray-200">历史订单汇总 ({7 + ordersCount} 笔)</span>
            </div>
            <ChevronRight size={14} className="text-gray-400" />
          </div>

          <div className="flex items-center justify-between p-4 text-xs font-bold hover:bg-gray-50 dark:hover:bg-gray-950/40 cursor-pointer transition-colors duration-200">
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center">
                <Heart size={14} className="text-red-500" strokeWidth={2.5} />
              </div>
              <span className="text-gray-800 dark:text-gray-200">我的收藏店铺 (4 家)</span>
            </div>
            <ChevronRight size={14} className="text-gray-400" />
          </div>

          <div 
            onClick={() => setIsAddressSectionOpen(true)}
            className="flex items-center justify-between p-4 text-xs font-bold hover:bg-gray-50 dark:hover:bg-gray-950/40 cursor-pointer transition-all duration-200 active:scale-[0.99]"
          >
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-emerald-50 dark:bg-emerald-500/10 flex items-center justify-center">
                <MapPin size={14} className="text-emerald-500" strokeWidth={2.5} />
              </div>
              <span className="text-gray-800 dark:text-gray-200">常用收货地址管理</span>
            </div>
            <ChevronRight size={14} className="text-gray-400" />
          </div>

        </div>

      </div>

      </div> {/* End of Scrollable Container */}

      {/* Aesthetic footer credits */}
      <div className="py-8 text-center text-[10px] text-gray-400 dark:text-gray-500 font-bold tracking-wide mt-auto shrink-0 uppercase">
        © 2026 连山壮族瑶族自治县 · 智慧同城生活平台
      </div>

      {/* Slide-over Panel: Address Management */}
      <AnimatePresence>
        {isAddressSectionOpen && (
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 26, stiffness: 220 }}
            className="absolute inset-0 bg-gray-50 dark:bg-black z-45 flex flex-col overflow-hidden"
          >
            {/* Header */}
            <div className="p-3 bg-gradient-to-br from-red-500 to-indigo-600 text-white shrink-0 shadow-md flex items-center justify-between">
              <button
                onClick={() => setIsAddressSectionOpen(false)}
                className="p-1.5 rounded-full hover:bg-white/10 transition cursor-pointer flex items-center"
              >
                <ArrowLeft size={22} />
              </button>
              <h2 className="text-sm font-black tracking-wide">我的收货地址</h2>
              <button 
                onClick={handleOpenAddAddress}
                className="px-2.5 py-1 rounded-lg bg-white/20 hover:bg-white/30 text-white font-extrabold text-[10px] flex items-center gap-1 cursor-pointer transition active:scale-95"
              >
                <Plus size={11} />
                <span>新增地址</span>
              </button>
            </div>

            {/* Address List */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {addresses.length === 0 ? (
                <div className="text-center py-16 flex flex-col items-center justify-center text-gray-400 dark:text-gray-500 bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl p-6 shadow-sm">
                  <MapPin size={32} className="opacity-20 mb-2.5 text-red-500 animate-bounce" />
                  <p className="text-xs font-black">暂无收货地址</p>
                  <p className="text-[10px] opacity-70 mt-1">您还没有添加任何配送地址，点击右上角立即新增一个吧！</p>
                </div>
              ) : (
                addresses.map((addr) => (
                  <div 
                    key={addr.id}
                    className={`p-3 rounded-[14px] bg-white dark:bg-gray-900 border transition-all duration-300 relative shadow-sm ${
                      addr.isDefault 
                        ? 'border-red-500/50 dark:border-red-500/40 shadow-blue-500/5' 
                        : 'border-gray-100 dark:border-gray-800 hover:border-gray-200 dark:hover:border-gray-750'
                    }`}
                  >
                    {/* Top row: Name & Phone & Tag */}
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-black text-gray-900 dark:text-white">
                          {addr.name}
                        </span>
                        <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
                          {addr.phone}
                        </span>
                        {/* Tag */}
                        <span className={`px-1.5 py-0.5 text-[8px] font-black rounded ${
                          addr.tag === '家' ? 'bg-green-500/10 text-green-500' :
                          addr.tag === '公司' ? 'bg-red-500/10 text-red-500' :
                          addr.tag === '学校' ? 'bg-amber-500/10 text-amber-500' :
                          'bg-purple-500/10 text-purple-500'
                        }`}>
                          {addr.tag}
                        </span>
                      </div>

                      {/* Default badge */}
                      {addr.isDefault && (
                        <span className="px-1.5 py-0.5 text-[8px] font-black bg-red-500 text-white rounded shadow-sm">
                          默认地址
                        </span>
                    )}
                    </div>

                    {/* Detailed Address */}
                    <p className="text-[11px] text-gray-600 dark:text-gray-300 mt-2 font-medium leading-relaxed">
                      {addr.address}
                    </p>

                    {/* Actions bar */}
                    <div className="mt-3 pt-2.5 border-t border-dashed border-gray-50 dark:border-gray-800/80 flex items-center justify-between">
                      {/* Set as Default button */}
                      {!addr.isDefault ? (
                        <button
                          onClick={(e) => handleSetDefaultAddress(addr.id, e)}
                          className="text-[10px] text-gray-400 hover:text-red-500 font-extrabold flex items-center gap-1 transition cursor-pointer"
                        >
                          <Check size={11} />
                          <span>设为默认</span>
                        </button>
                      ) : (
                        <span className="text-[10px] text-red-500 font-extrabold flex items-center gap-1">
                          <CheckCheck size={11} />
                          <span>当前默认</span>
                        </span>
                    )}

                      {/* Edit / Delete buttons */}
                      <div className="flex items-center gap-3">
                        <button
                          onClick={(e) => handleOpenEditAddress(addr, e)}
                          className="text-[10px] text-gray-400 hover:text-amber-500 font-extrabold flex items-center gap-1 transition cursor-pointer"
                        >
                          <Edit size={11} />
                          <span>编辑</span>
                        </button>
                        <button
                          onClick={(e) => handleDeleteAddress(addr.id, e)}
                          className="text-[10px] text-gray-400 hover:text-red-500 font-extrabold flex items-center gap-1 transition cursor-pointer"
                        >
                          <Trash2 size={11} />
                          <span>删除</span>
                        </button>
                      </div>
                    </div>
                  </div>
                ))
            )}
            </div>

            {/* Bottom Safe Action Banner */}
            <div className="p-4 bg-white dark:bg-gray-900 border-t border-gray-100 dark:border-gray-800 shrink-0">
              <button
                onClick={handleOpenAddAddress}
                className="w-full py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-bold text-xs shadow-md transition duration-200 cursor-pointer flex items-center justify-center gap-1.5 active:scale-98"
              >
                <Plus size={13} />
                <span>添加新收货地址</span>
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Modal Dialog: Add/Edit Address */}
      <AnimatePresence>
        {isAddressModalOpen && (
          <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, y: 100 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 100 }}
              transition={{ type: 'spring', damping: 25, stiffness: 250 }}
              className={`w-full max-w-md rounded-t-3xl sm:rounded-2xl shadow-2xl overflow-hidden border p-5 space-y-4 ${
                isDarkMode 
                  ? 'bg-gray-900 border-gray-800 text-white' 
                  : 'bg-white border-gray-100 text-gray-900'
              }`}
            >
              {/* Form Title */}
              <div className="flex items-center justify-between pb-2 border-b border-gray-50 dark:border-gray-800/50">
                <h3 className="text-xs font-black tracking-wider uppercase text-gray-400">
                  {editingAddress ? '✏️ 编辑收货地址' : '➕ 新增收货地址'}
                </h3>
                <button
                  onClick={() => setIsAddressModalOpen(false)}
                  className="p-1 rounded-full text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition cursor-pointer"
                >
                  <X size={15} />
                </button>
              </div>

              {/* Form Body */}
              <div className="space-y-3.5 text-xs">
                {/* Error message */}
                {formError && (
                  <div className="p-2.5 rounded-xl bg-red-500/10 text-red-500 font-bold border border-red-500/20 text-[10px]">
                    ⚠️ {formError}
                  </div>
              )}

                {/* Recipient Name */}
                <div className="space-y-1">
                  <label className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider">收货人姓名</label>
                  <input
                    type="text"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="请输入姓名，例如：雷大叔"
                    className="w-full p-2.5 rounded-xl border bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 text-gray-850 dark:text-gray-100 focus:outline-none focus:ring-1 focus:ring-red-500/50 font-semibold"
                  />
                </div>

                {/* Contact Phone */}
                <div className="space-y-1">
                  <label className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider">手机号码</label>
                  <input
                    type="tel"
                    value={formPhone}
                    onChange={(e) => setFormPhone(e.target.value)}
                    placeholder="请输入11位手机号码"
                    maxLength={11}
                    className="w-full p-2.5 rounded-xl border bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 text-gray-850 dark:text-gray-100 focus:outline-none focus:ring-1 focus:ring-red-500/50 font-semibold"
                  />
                </div>

                {/* Tags Selector */}
                <div className="space-y-1">
                  <label className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider">标签（快捷标记）</label>
                  <div className="grid grid-cols-4 gap-2">
                    {(['家', '公司', '学校', '其他'] as const).map((tag) => (
                      <button
                        key={tag}
                        type="button"
                        onClick={() => setFormTag(tag)}
                        className={`py-1.5 rounded-xl border text-[11px] font-extrabold cursor-pointer transition-all duration-200 ${
                          formTag === tag
                            ? 'bg-red-500 text-white border-transparent shadow'
                            : 'bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 hover:bg-gray-100 text-gray-600 dark:text-gray-300'
                        }`}
                      >
                        {tag}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Detailed Address Str */}
                <div className="space-y-1">
                  <label className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider">详细配送地址</label>
                  <textarea
                    value={formAddressStr}
                    onChange={(e) => setFormAddressStr(e.target.value)}
                    placeholder="请输入完整的收货地址，例如：广东省清远市连山壮族瑶族自治县..."
                    rows={3}
                    maxLength={200}
                    className="w-full p-2.5 rounded-xl border bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 text-gray-850 dark:text-gray-100 focus:outline-none focus:ring-1 focus:ring-red-500/50 font-semibold resize-none leading-relaxed"
                  />
                </div>

                {/* Default Switch */}
                <div className="flex items-center justify-between p-2.5 rounded-xl bg-gray-50/50 dark:bg-gray-800/30 border border-gray-100 dark:border-gray-800/20">
                  <div className="space-y-0.5">
                    <span className="text-[11px] font-black">设为默认配送地址</span>
                    <p className="text-[9px] text-gray-400">下单时将默认匹配及选中此地址</p>
                  </div>
                  <input
                    type="checkbox"
                    checked={formIsDefault}
                    onChange={(e) => setFormIsDefault(e.target.checked)}
                    className="w-4 h-4 text-red-600 bg-gray-100 border-gray-300 rounded focus:ring-red-500 dark:focus:ring-red-600 dark:ring-offset-gray-850 focus:ring-2 dark:bg-gray-700 dark:border-gray-600 cursor-pointer"
                  />
                </div>
              </div>

              {/* Form Actions */}
              <div className="flex items-center gap-3 pt-3 border-t border-gray-50 dark:border-gray-800/50">
                <button
                  type="button"
                  onClick={() => setIsAddressModalOpen(false)}
                  className="flex-1 py-2 rounded-xl bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 font-extrabold text-[11px] hover:bg-gray-200 dark:hover:bg-gray-750 cursor-pointer transition"
                >
                  取消
                </button>
                <button
                  type="button"
                  onClick={handleSaveAddress}
                  className="flex-1 py-2 rounded-xl bg-red-500 hover:bg-red-600 text-white font-extrabold text-[11px] cursor-pointer transition shadow-md active:scale-98"
                >
                  保存并应用
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Settings Modal (Yelp Style) */}
      <AnimatePresence>
        {showSettings && (
          <motion.div
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 50 }}
            transition={{ duration: 0.2 }}
            className={`absolute inset-0 z-[100] flex flex-col ${isDarkMode ? 'bg-gray-950 text-gray-100' : 'bg-gray-50 text-gray-900'}`}
          >
            {/* Header */}
            <div className="flex items-center px-4 py-4 bg-[#FF1A1A] text-white shrink-0 shadow-sm relative">
              <button
                onClick={() => {
                  if (activeSettingView === 'main') {
                    setShowSettings(false);
                  } else {
                    setActiveSettingView('main');
                  }
                }}
                className="absolute left-4 p-1 rounded-full hover:bg-white/20 transition cursor-pointer"
              >
                <ArrowLeft size={22} strokeWidth={2.5} />
              </button>
              <h2 className="flex-1 text-center font-bold text-[16px] tracking-wide">
                {activeSettingView === 'main' && t('settings.title')}
                {activeSettingView === 'profile' && t('settings.profile')}
                {activeSettingView === 'notifications' && t('settings.notifications')}
                {activeSettingView === 'privacy' && t('settings.privacy')}
                {activeSettingView === 'locations' && t('settings.locations')}
                {activeSettingView === 'terms' && t('settings.terms')}
                {activeSettingView === 'policy' && t('settings.policy')}
              </h2>
            </div>
            
            {/* Scrollable Content */}
            <div className="flex-1 overflow-y-auto relative">
              <AnimatePresence mode="wait">
                {activeSettingView === 'main' && (
                  <motion.div
                    key="main"
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.2 }}
                    className="pb-8 pt-2"
                  >
                    <div className="mt-4 mb-2 px-4 text-[12px] font-extrabold text-gray-500 uppercase tracking-wider">{t('settings.account')}</div>
                    <div className={`mx-4 rounded-xl overflow-hidden shadow-sm border ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <div onClick={() => setActiveSettingView('profile')} className={`flex items-center justify-between p-4 border-b cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50 ${isDarkMode ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3">
                          <User size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.profile')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                      <div onClick={() => setActiveSettingView('notifications')} className={`flex items-center justify-between p-4 border-b cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50 ${isDarkMode ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3">
                          <Bell size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.notifications')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                      <div onClick={() => setActiveSettingView('privacy')} className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
                        <div className="flex items-center gap-3">
                          <Lock size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.privacy')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                    </div>

                    <div className="mt-6 mb-2 px-4 text-[12px] font-extrabold text-gray-500 uppercase tracking-wider">{t('settings.preferences')}</div>
                    <div className={`mx-4 rounded-xl overflow-hidden shadow-sm border ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <div onClick={() => setActiveSettingView('locations')} className={`flex items-center justify-between p-4 border-b cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50 ${isDarkMode ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3">
                          <MapPin size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.locations')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                      <div 
                        onClick={() => setLanguage(language === 'zh' ? 'en' : 'zh')}
                        className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50"
                      >
                        <div className="flex items-center gap-3">
                          <Globe size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.language')}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-[12px] font-medium text-gray-500">{language === 'zh' ? '简体中文' : 'English'}</span>
                          <ChevronRight size={18} className="text-gray-300" />
                        </div>
                      </div>
                    </div>

                    <div className="mt-6 mb-2 px-4 text-[12px] font-extrabold text-gray-500 uppercase tracking-wider">{t('settings.about')}</div>
                    <div className={`mx-4 rounded-xl overflow-hidden shadow-sm border ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <div onClick={() => setActiveSettingView('terms')} className={`flex items-center justify-between p-4 border-b cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50 ${isDarkMode ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3">
                          <FileText size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.terms')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                      <div onClick={() => setActiveSettingView('policy')} className={`flex items-center justify-between p-4 border-b cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50 ${isDarkMode ? 'border-gray-800' : 'border-gray-100'}`}>
                        <div className="flex items-center gap-3">
                          <ShieldCheck size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.policy')}</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-300" />
                      </div>
                      <div className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
                        <div className="flex items-center gap-3">
                          <Info size={18} className="text-gray-400" />
                          <span className="font-bold text-[14px]">{t('settings.version')}</span>
                        </div>
                        <span className="text-[12px] font-bold text-gray-400">1.0.0</span>
                      </div>
                    </div>
                    
                    <div className="mt-8 px-4">
                      <button 
                        className="w-full py-3.5 rounded-xl bg-gray-200/50 dark:bg-gray-800 text-[#FF1A1A] font-extrabold text-[14px] hover:bg-gray-200 dark:hover:bg-gray-700 transition flex items-center justify-center gap-2 cursor-pointer shadow-sm border border-gray-200/50 dark:border-gray-700"
                      >
                        <LogOut size={18} strokeWidth={2.5} />
                        {t('settings.logout')}
                      </button>
                    </div>
                  </motion.div>
                )}

                {activeSettingView === 'profile' && (
                  <motion.div
                    key="profile"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-4"
                  >
                    <div className="flex flex-col items-center gap-3 mb-8 mt-4">
                      <div className="relative">
                        <img 
                          src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80" 
                          alt="Avatar" 
                          className="w-24 h-24 rounded-full object-cover border-4 border-white shadow-sm"
                        />
                        <button className="absolute bottom-0 right-0 bg-red-500 text-white p-1.5 rounded-full shadow-md border-2 border-white">
                          <User size={14} />
                        </button>
                      </div>
                      <button className="text-red-500 font-bold text-xs">{t('settings.profile')}</button>
                    </div>

                    <div className="space-y-4">
                      <div className="space-y-1.5">
                        <label className="text-[11px] font-bold text-gray-400 uppercase ml-1">Username</label>
                        <input type="text" defaultValue="LianShan_A9" className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl px-4 py-3 text-sm font-bold focus:outline-none focus:ring-2 focus:ring-red-500/20 transition-all" />
                      </div>
                      <div className="space-y-1.5">
                        <label className="text-[11px] font-bold text-gray-400 uppercase ml-1">Email</label>
                        <input type="email" defaultValue="user@example.com" className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl px-4 py-3 text-sm font-bold focus:outline-none focus:ring-2 focus:ring-red-500/20 transition-all" />
                      </div>
                      <div className="space-y-1.5">
                        <label className="text-[11px] font-bold text-gray-400 uppercase ml-1">Phone</label>
                        <input type="tel" defaultValue="+86 138 0000 0000" className="w-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl px-4 py-3 text-sm font-bold focus:outline-none focus:ring-2 focus:ring-red-500/20 transition-all" />
                      </div>
                      <button className="w-full py-3.5 mt-4 rounded-xl bg-[#FF1A1A] text-white font-extrabold text-[14px] hover:bg-red-600 transition shadow-sm">
                        Save Changes
                      </button>
                    </div>
                  </motion.div>
                )}

                {activeSettingView === 'notifications' && (
                  <motion.div
                    key="notifications"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-4"
                  >
                    <div className="space-y-4">
                      <div className={`p-4 rounded-xl shadow-sm border flex items-center justify-between ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                        <div>
                          <h4 className="font-bold text-sm">Order Updates</h4>
                          <p className="text-[11px] text-gray-400 mt-0.5">Get notified about your order status</p>
                        </div>
                        <div className="w-10 h-6 bg-red-500 rounded-full relative cursor-pointer">
                          <div className="absolute right-1 top-1 w-4 h-4 bg-white rounded-full"></div>
                        </div>
                      </div>
                      <div className={`p-4 rounded-xl shadow-sm border flex items-center justify-between ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                        <div>
                          <h4 className="font-bold text-sm">Promotions</h4>
                          <p className="text-[11px] text-gray-400 mt-0.5">Discounts and special offers</p>
                        </div>
                        <div className="w-10 h-6 bg-gray-300 dark:bg-gray-700 rounded-full relative cursor-pointer">
                          <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full"></div>
                        </div>
                      </div>
                      <div className={`p-4 rounded-xl shadow-sm border flex items-center justify-between ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                        <div>
                          <h4 className="font-bold text-sm">System Alerts</h4>
                          <p className="text-[11px] text-gray-400 mt-0.5">Important account and security updates</p>
                        </div>
                        <div className="w-10 h-6 bg-red-500 rounded-full relative cursor-pointer">
                          <div className="absolute right-1 top-1 w-4 h-4 bg-white rounded-full"></div>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                )}

                {activeSettingView === 'privacy' && (
                  <motion.div
                    key="privacy"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-4 space-y-4"
                  >
                    <div className={`p-4 rounded-xl shadow-sm border flex flex-col gap-3 ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <h4 className="font-bold text-sm">Change Password</h4>
                      <input type="password" placeholder="Current Password" className="w-full bg-gray-50 dark:bg-gray-950 border border-gray-200 dark:border-gray-800 rounded-lg px-3 py-2 text-sm focus:outline-none" />
                      <input type="password" placeholder="New Password" className="w-full bg-gray-50 dark:bg-gray-950 border border-gray-200 dark:border-gray-800 rounded-lg px-3 py-2 text-sm focus:outline-none" />
                      <button className="py-2 bg-gray-900 dark:bg-white text-white dark:text-gray-900 rounded-lg font-bold text-xs mt-1">Update Password</button>
                    </div>
                    
                    <div className={`p-4 rounded-xl shadow-sm border flex items-center justify-between ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <div>
                        <h4 className="font-bold text-sm">Two-Factor Auth</h4>
                        <p className="text-[11px] text-gray-400 mt-0.5">Extra layer of security</p>
                      </div>
                      <div className="w-10 h-6 bg-gray-300 dark:bg-gray-700 rounded-full relative cursor-pointer">
                        <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full"></div>
                      </div>
                    </div>
                  </motion.div>
                )}

                {activeSettingView === 'locations' && (
                  <motion.div
                    key="locations"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-4 space-y-4"
                  >
                    <div className={`p-4 rounded-xl shadow-sm border ${isDarkMode ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100'}`}>
                      <div className="flex items-start justify-between border-b border-gray-100 dark:border-gray-800 pb-3 mb-3">
                        <div className="flex items-start gap-3">
                          <MapPin size={18} className="text-red-500 mt-0.5" />
                          <div>
                            <div className="flex items-center gap-2">
                              <h4 className="font-bold text-sm">Home</h4>
                              <span className="bg-red-100 text-red-600 text-[9px] px-1.5 py-0.5 rounded font-bold">Default</span>
                            </div>
                            <p className="text-[11px] text-gray-500 mt-1">123 Example Street, Apt 4B, City</p>
                            <p className="text-[10px] text-gray-400 mt-0.5">Contact: LianShan (+86 138****0000)</p>
                          </div>
                        </div>
                        <button className="text-gray-400 hover:text-red-500"><ShieldCheck size={14} /></button>
                      </div>
                      <div className="flex items-start gap-3">
                        <MapPin size={18} className="text-gray-400 mt-0.5" />
                        <div>
                          <h4 className="font-bold text-sm">Office</h4>
                          <p className="text-[11px] text-gray-500 mt-1">456 Business Road, Floor 12, City</p>
                          <p className="text-[10px] text-gray-400 mt-0.5">Contact: LianShan (+86 138****0000)</p>
                        </div>
                      </div>
                    </div>
                    
                    <button className="w-full py-3 rounded-xl border-2 border-dashed border-gray-300 dark:border-gray-700 text-gray-500 dark:text-gray-400 font-bold text-xs flex items-center justify-center gap-2 hover:border-red-500 hover:text-red-500 transition">
                      <MapPin size={14} />
                      Add New Address
                    </button>
                  </motion.div>
                )}

                {activeSettingView === 'terms' && (
                  <motion.div
                    key="terms"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-6 text-sm text-gray-600 dark:text-gray-400 space-y-4"
                  >
                    <h3 className="text-lg font-black text-gray-900 dark:text-white mb-4">Terms of Service</h3>
                    <p>Welcome to LianShan Local Services. By using our application, you agree to these terms.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">1. User Account</p>
                    <p>You are responsible for maintaining the confidentiality of your account credentials. You must be at least 18 years old to use certain premium services.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">2. Services Provided</p>
                    <p>LianShan provides a local marketplace connecting users with merchants, delivery personnel, and local service providers. We do not guarantee the quality of third-party services.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">3. Prohibited Conduct</p>
                    <p>You agree not to engage in fraudulent activities, spam, or any behavior that violates local laws while using the platform.</p>
                  </motion.div>
                )}

                {activeSettingView === 'policy' && (
                  <motion.div
                    key="policy"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="p-6 text-sm text-gray-600 dark:text-gray-400 space-y-4"
                  >
                    <h3 className="text-lg font-black text-gray-900 dark:text-white mb-4">Privacy Policy</h3>
                    <p>Your privacy is important to us. This policy explains how we collect and use your data.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">Data Collection</p>
                    <p>We collect information you provide directly, such as your name, email, phone number, and delivery addresses.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">Data Usage</p>
                    <p>Your data is used strictly for fulfilling your orders, improving our app experience, and providing customer support. We do not sell your personal data to third parties.</p>
                    <p className="font-bold text-gray-800 dark:text-gray-200 mt-4">Location Services</p>
                    <p>We require location access to track deliveries and show you nearby merchants. You can disable this in your device settings.</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      
    </div>
  );
}