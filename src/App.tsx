/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Compass, 
  Search, 
  Moon, 
  Sun, 
  MapPin, 
  Bike, 
  Sparkles, 
  ChevronRight, 
  Clock, 
  ShoppingBag,
  Grid,
  Filter,
  CheckCircle2,
  Home,
  User,
  ShoppingCart,
  FileText,
  Plus,
  Grid2X2,
  PlusCircle,
  Briefcase,
  Wrench,
  Utensils,
  Truck,
  Zap,
  Gamepad2,
  Recycle,
  MonitorPlay,
  HeartHandshake,
  Megaphone,
  X,
  Carrot, Flower2, Plug, GraduationCap, Package, Star, Heart,
} from 'lucide-react';
import { Merchant, CartItem } from './types';
import MerchantCard from './components/MerchantCard';
import OrderDetailSheet from './components/OrderDetailSheet';
import PaymentModal from './components/PaymentModal';
import DeliveryTracker from './components/DeliveryTracker';
import UserProfile from './components/UserProfile';
import CartView from './components/CartView';
import OrderListView from './components/OrderListView';
import PublishView from './components/PublishView';

import SubscriptionModal from './components/SubscriptionModal';
import { useLanguage } from './contexts/LanguageContext';

export default function App() {
  const { t } = useLanguage();
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [selectedMerchant, setSelectedMerchant] = useState<Merchant | null>(null);
  const [currentScreen, setCurrentScreen] = useState<'home' | 'detail' | 'cart' | 'tracker' | 'profile' | 'orders' | 'publish'>('home');
  const [slideDirection, setSlideDirection] = useState<'forward' | 'backward'>('forward');
  
  // Membership State
  const [membershipTier, setMembershipTier] = useState<'free' | 'vip' | 'premium'>('free');
  const [publishedCount, setPublishedCount] = useState(0);
  const [isSubscriptionModalOpen, setIsSubscriptionModalOpen] = useState(false);

  // Track session completed orders for dynamic chart updates
  const [ordersCount, setOrdersCount] = useState<number>(0);
  const [currentOrderAmount, setCurrentOrderAmount] = useState<number>(0);

  // Global cart quantity count
  const [cartCount, setCartCount] = useState<number>(0);

  const updateCartCount = () => {
    try {
      const saved = localStorage.getItem('user_carts');
      if (saved) {
        const carts = JSON.parse(saved);
        const count = Object.values(carts).reduce((sum: number, cart: any) => {
          return sum + (cart.items || []).reduce((itemSum: number, item: any) => itemSum + (item.quantity || 0), 0);
        }, 0);
        setCartCount(count);
      } else {
        setCartCount(0);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    updateCartCount();
    window.addEventListener('storage', updateCartCount);
    return () => {
      window.removeEventListener('storage', updateCartCount);
    };
  }, []);

  const screenIndex: Record<string, number> = useMemo(() => ({
    home: 0,
    orders: 1,
    publish: 2,
    cart: 3,
    profile: 4,
    detail: 5,
    tracker: 6,
  }), []);

  const navigateToScreen = (screen: 'home' | 'detail' | 'cart' | 'tracker' | 'profile' | 'orders' | 'publish') => {
    const currentIndex = screenIndex[currentScreen];
    const nextIndex = screenIndex[screen];
    setSlideDirection(nextIndex >= currentIndex ? 'forward' : 'backward');
    setCurrentScreen(screen);
  };

  const screenVariants = {
    initial: (direction: 'forward' | 'backward') => ({
      x: direction === 'forward' ? '100%' : '-100%',
      opacity: 0,
    }),
    animate: {
      x: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 350,
        damping: 32,
      },
    },
    exit: (direction: 'forward' | 'backward') => ({
      x: direction === 'forward' ? '-100%' : '100%',
      opacity: 0,
      transition: {
        type: 'spring',
        stiffness: 350,
        damping: 32,
      },
    }),
  };

  const [activeOrderId, setActiveOrderId] = useState<string | null>(null);

  // Filter/Search states
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [sortBy, setSortBy] = useState<'default' | 'distance' | 'sales' | 'rating'>('default');

  // Interactive AI states
  const [highlightedItemId, setHighlightedItemId] = useState<string | null>(null);

  // Dark Mode adaptation
  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  // Payment popup states
  const [isPayOpen, setIsPayOpen] = useState<boolean>(false);
  const [pendingOrderDetails, setPendingOrderDetails] = useState<{
    cart: CartItem[];
    amount: number;
    merchantId: string;
    merchantName: string;
    deliveryAddress: { name: string; phone: string; address: string };
  } | null>(null);

  // Banner states
  

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkMode]);

  // Fetch initial merchants from full-stack backend
  const fetchMerchants = async () => {
    try {
      const res = await fetch('/api/merchants');
      if (res.ok) {
        const data = await res.json();
        setMerchants(data);
      }
    } catch (e) {
      console.error('Failed to load merchants from server, using local fallback:', e);
    }
  };

  useEffect(() => {
    fetchMerchants();
  }, []);

  // Filter & Sort logic
  
  const recommendedMerchants = useMemo(() => {
    // 模拟基于用户历史购买或点击记录的推荐算法
    return [...merchants].sort((a, b) => b.rating - a.rating).slice(0, 3);
  }, [merchants]);

  const filteredMerchants = useMemo(() => {
    let result = [...merchants];

    // Category filter
    if (selectedCategory !== 'all') {
      result = result.filter(m => m.category === selectedCategory);
    }

    // Search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      result = result.filter(m => 
        m.name.toLowerCase().includes(query) || 
        m.tags.some(tag => tag.toLowerCase().includes(query)) ||
        m.items.some(item => item.name.toLowerCase().includes(query))
      );
    }

    // Sorting
    if (sortBy === 'distance') {
      result.sort((a, b) => a.distance - b.distance);
    } else if (sortBy === 'sales') {
      result.sort((a, b) => b.sales - a.sales);
    } else if (sortBy === 'rating') {
      result.sort((a, b) => b.rating - a.rating);
    }

    return result;
  }, [merchants, selectedCategory, searchQuery, sortBy]);

  // Handle Order Placement & checkout
  const handleCheckoutInit = (
    cart: CartItem[], 
    paymentMethod: 'alipay' | 'wechat' | 'wallet',
    deliveryAddress: { name: string, phone: string, address: string }
  ) => {
    if (!selectedMerchant) return;
    
    const itemsTotal = cart.reduce((sum, c) => sum + c.item.price * c.quantity, 0);
    const finalAmount = itemsTotal + selectedMerchant.deliveryFee;

    setPendingOrderDetails({
      cart,
      amount: finalAmount,
      merchantId: selectedMerchant.id,
      merchantName: selectedMerchant.name,
      deliveryAddress
    });

    setIsPayOpen(true);
  };

  // On secure payment confirmation, send to backend API
  const handlePaymentCompleted = async (method: 'alipay' | 'wechat' | 'wallet') => {
    if (!pendingOrderDetails) return;

    try {
      const response = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          merchantId: pendingOrderDetails.merchantId,
          items: pendingOrderDetails.cart,
          totalAmount: pendingOrderDetails.amount,
          deliveryFee: selectedMerchant?.deliveryFee || 0,
          paymentMethod: method,
          deliveryAddress: pendingOrderDetails.deliveryAddress
        })
      });

      if (!response.ok) {
        throw new Error('订单创建失败');
      }

      const orderData = await response.json();
      if (orderData.success) {
        // Clear merchant cart from global user_carts
        try {
          const savedStr = localStorage.getItem('user_carts');
          if (savedStr) {
            const saved = JSON.parse(savedStr);
            delete saved[pendingOrderDetails.merchantId];
            localStorage.setItem('user_carts', JSON.stringify(saved));
            window.dispatchEvent(new Event('storage'));
          }
        } catch (err) {
          console.error('Failed to clear checked-out cart:', err);
        }

        setOrdersCount(prev => prev + 1);
        setCurrentOrderAmount(prev => prev + pendingOrderDetails.amount);
        setActiveOrderId(orderData.order.id);
        setIsPayOpen(false);
        setPendingOrderDetails(null);
        navigateToScreen('tracker');
      }
    } catch (e) {
      alert('订单创建出错，请重试');
    }
  };

  // AI assistant recommendations click handler
  const handleSelectAiItem = (merchantId: string, itemId: string) => {
    const targetMerchant = merchants.find(m => m.id === merchantId);
    if (targetMerchant) {
      setSelectedMerchant(targetMerchant);
      setHighlightedItemId(itemId);
      navigateToScreen('detail');
    }
  };

  return (
    <div className={`h-[100dvh] w-full font-sans antialiased select-none flex flex-col overflow-hidden transition-colors duration-300 ${
      isDarkMode ? 'bg-black text-gray-100' : 'bg-gray-50 text-gray-900'
    }`}>
      
      {/* Main Container constrained to Mobile layout for genuine App experience */}
      <div className="w-full max-w-[430px] mx-auto flex-1 bg-white dark:bg-gray-950 flex flex-col shadow-2xl relative h-[100dvh] overflow-hidden">
        
        <AnimatePresence mode="wait" initial={false} custom={slideDirection}>
          {currentScreen === 'home' && (
            <motion.div
              key="home"
              custom={slideDirection}
              variants={screenVariants}
              initial="initial"
              animate="animate"
              exit="exit"
              className="flex-1 flex flex-col min-h-0 w-full overflow-y-auto pb-6"
            >
            {/* Top Navigation Header bar */}
            <header className="sticky top-0 z-20 px-4 pt-3 pb-3 bg-[#FF1A1A] dark:bg-[#D32323] text-white shrink-0 shadow-sm">
              <div className="flex items-center justify-between">
                {/* Yelp Style Title */}
                <div className="shrink-0 flex items-center gap-1.5">
                  <div className="bg-white text-[#FF1A1A] p-0.5 rounded-sm font-black text-xs leading-none">yelp</div>
                  <h1 className="text-[15px] font-black tracking-tight">
                    连山
                  </h1>
                </div>

                {/* Dark mode switcher */}
                <button
                  onClick={() => setIsDarkMode(!isDarkMode)}
                  className="p-1.5 rounded-full hover:bg-white/20 transition cursor-pointer"
                  title={isDarkMode ? '切换日间模式' : '切换暗黑模式'}
                >
                  {isDarkMode ? <Sun size={16} strokeWidth={2.5} /> : <Moon size={16} strokeWidth={2.5} />}
                </button>
              </div>

              {/* Location & Search bar */}
              <div className="mt-3 flex flex-col gap-2">
                <div className="flex items-center gap-1 cursor-pointer max-w-[260px] opacity-90">
                  <MapPin size={12} strokeWidth={3} className="shrink-0" />
                  <span className="font-bold text-[11px] truncate">
                    连山壮族瑶族自治县 瑶香苑小区
                  </span>
                  <ChevronRight size={13} strokeWidth={3} />
                </div>
                
                {/* Main search bar */}
                <div className="relative flex items-center w-full h-[34px] mt-1">
                  <Search size={15} strokeWidth={2.5} className="absolute left-2.5 text-gray-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder={t('home.search')}
                    className="w-full h-full text-[12px] pl-8 pr-7 rounded-md border-none bg-white text-gray-900 outline-none shadow-sm placeholder-gray-400 font-medium focus:bg-gray-50 transition-colors"
                  />
                  {searchQuery && (
                    <button 
                      onClick={() => setSearchQuery('')}
                      className="absolute right-2.5 text-gray-400 hover:text-gray-600 cursor-pointer w-5 h-5 flex items-center justify-center"
                    >
                      <X size={14} strokeWidth={3} />
                    </button>
                  )}
                </div>
              </div>
            </header>

            {/* Active Delivery Status Banner */}
            {activeOrderId && (
              <div className="px-4 pt-3 shrink-0">
                <button
                  onClick={() => navigateToScreen('tracker')}
                  className="w-full p-3.5 rounded-2xl bg-gradient-to-r from-red-500 to-indigo-600 text-white flex items-center justify-between shadow-lg hover:scale-[1.01] active:scale-[0.99] transition duration-200 cursor-pointer"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center text-lg animate-bounce">
                      🛵
                    </div>
                    <div className="text-left space-y-0.5">
                      <p className="text-xs font-black flex items-center gap-1.5">
                        <span>同城特惠配送中 ({activeOrderId})</span>
                        <span className="inline-flex h-2 w-2 rounded-full bg-pink-400 animate-ping"></span>
                      </p>
                      <p className="text-[10px] text-red-100 font-medium opacity-90">
                        特派骑手阿力正飞速向您奔驰中，点击查看实时地图
                      </p>
                    </div>
                  </div>
                  <ChevronRight size={16} className="text-white/80" />
                </button>
              </div>
            )}

            {/* Goods and Services Panel */}
            <div className="px-3 py-1.5 shrink-0">
              <div className="bg-white dark:bg-gray-900 rounded-[12px] shadow-sm border border-gray-100 dark:border-gray-800 p-2.5">
                <div className="flex items-center justify-between mb-2.5 px-1">
                  <h2 className="text-[13px] font-extrabold tracking-tight text-gray-900 dark:text-white">商品和服务</h2>
                  <span className="text-[9px] text-red-600 dark:text-red-400 font-extrabold bg-red-50 dark:bg-red-500/10 px-2 py-[2px] rounded-full border border-red-100 dark:border-red-900/30">同城配送上门</span>
                </div>
                <div className="grid grid-cols-4 gap-y-3 gap-x-1.5 py-0.5 px-1 text-center justify-items-center">
                  
                  <button onClick={() => setSelectedCategory('job')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-red-50 dark:bg-red-500/10 flex items-center justify-center text-red-500 border border-red-100 dark:border-red-900/30">
                      <Briefcase size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">招聘</span>
                  </button>

                  <button onClick={() => setSelectedCategory('house')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-emerald-50 dark:bg-emerald-500/10 flex items-center justify-center text-emerald-500 border border-emerald-100 dark:border-emerald-900/30">
                      <Home size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">房屋租售</span>
                  </button>

                  <button onClick={() => setSelectedCategory('housekeeping')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-purple-50 dark:bg-purple-500/10 flex items-center justify-center text-purple-500 border border-purple-100 dark:border-purple-900/30">
                      <Sparkles size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">家政保洁</span>
                  </button>

                  <button onClick={() => setSelectedCategory('maintenance')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-cyan-50 dark:bg-cyan-500/10 flex items-center justify-center text-cyan-500 border border-cyan-100 dark:border-cyan-900/30">
                      <Zap size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">水电维修</span>
                  </button>

                  <button onClick={() => setSelectedCategory('moving')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-amber-50 dark:bg-amber-500/10 flex items-center justify-center text-amber-500 border border-amber-100 dark:border-amber-900/30">
                      <Truck size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">货运搬家</span>
                  </button>

                  <button onClick={() => setSelectedCategory('veggies')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-green-50 dark:bg-green-500/10 flex items-center justify-center text-green-500 border border-green-100 dark:border-green-900/30">
                      <Carrot size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">水果蔬菜</span>
                  </button>

                  <button onClick={() => setSelectedCategory('secondhand')} className="flex flex-col items-center gap-1 shrink-0 w-[54px] transition cursor-pointer hover:opacity-80">
                    <div className="w-8 h-8 rounded-[10px] bg-orange-50 dark:bg-orange-500/10 flex items-center justify-center text-orange-500 border border-orange-100 dark:border-orange-900/30">
                      <Recycle size={16} strokeWidth={2} />
                    </div>
                    <span className="text-[10px] font-extrabold text-gray-700 dark:text-gray-300 truncate w-full">个人闲置</span>
                  </button>

                </div>
              </div>
            </div>
            
            {/* Guess You Like (Smart Recommendations) */}
            {recommendedMerchants.length > 0 && (
              <div className="px-3 pt-1.5 pb-1 shrink-0">
                <div className="flex items-center justify-between mb-2 px-1">
                  <h2 className="text-[13px] font-extrabold tracking-tight text-gray-900 dark:text-white flex items-center gap-1">
                    <Heart size={14} className="text-[#FF1A1A] fill-[#FF1A1A]" />
                    猜你喜欢
                  </h2>
                  <span className="text-[9px] text-gray-500 font-medium">基于近期点击记录推荐</span>
                </div>
                <div className="flex gap-2.5 overflow-x-auto no-scrollbar px-1 pb-1.5">
                  {recommendedMerchants.map(merchant => (
                    <div 
                      key={merchant.id}
                      onClick={() => {
                        setHighlightedItemId(null);
                        setSelectedMerchant(merchant);
                        navigateToScreen('detail');
                      }}
                      className="w-[84px] shrink-0 bg-white dark:bg-gray-900 rounded-[10px] shadow-sm border border-gray-100 dark:border-gray-800 overflow-hidden cursor-pointer hover:shadow-md transition"
                    >
                      <div className="h-[54px] relative">
                        <img src={merchant.logo} alt={merchant.name} className="w-full h-full object-cover" />
                        <div className="absolute top-1 right-1 bg-black/60 backdrop-blur-sm px-1 py-[1px] rounded text-[7px] text-white font-bold flex items-center gap-[1px]">
                          <Star size={7} className="fill-amber-400 text-amber-400" />
                          {merchant.rating}
                        </div>
                      </div>
                      <div className="p-1.5">
                        <h3 className="text-[10px] font-extrabold text-gray-900 dark:text-white truncate mb-0.5">{merchant.name}</h3>
                        <div className="flex items-center gap-0.5 text-[8px] text-gray-500 mb-1">
                          <MapPin size={8} />
                          <span className="truncate">{merchant.distance} km</span>
                        </div>
                        <div className="flex flex-wrap gap-0.5 h-[12px] overflow-hidden">
                          {merchant.tags.slice(0, 1).map((tag, idx) => (
                            <span key={idx} className="bg-red-50 dark:bg-red-500/10 text-red-500 px-1 py-[0.5px] rounded-[3px] text-[7px] font-medium truncate max-w-full">
                              {tag}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Merchant list filters tab */}
            <div className="sticky top-[64px] z-10 bg-white dark:bg-gray-950 border-b border-gray-200 dark:border-gray-800 py-3 mt-2 shadow-sm">
              <div className="flex items-center gap-2 px-4 overflow-x-auto no-scrollbar">
                <button 
                  onClick={() => setSortBy('default')}
                  className={`whitespace-nowrap px-4 py-1.5 rounded-full text-[11px] font-bold transition-all shadow-sm ${sortBy === 'default' ? 'bg-[#FF1A1A] text-white' : 'bg-white border border-gray-300 text-gray-700 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-300'}`}
                >
                  Recommended
                </button>
                <button 
                  onClick={() => setSortBy('distance')}
                  className={`whitespace-nowrap px-4 py-1.5 rounded-full text-[11px] font-bold transition-all shadow-sm ${sortBy === 'distance' ? 'bg-[#FF1A1A] text-white' : 'bg-white border border-gray-300 text-gray-700 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-300'}`}
                >
                  Distance
                </button>
                <button 
                  onClick={() => setSortBy('rating')}
                  className={`whitespace-nowrap px-4 py-1.5 rounded-full text-[11px] font-bold transition-all shadow-sm ${sortBy === 'rating' ? 'bg-[#FF1A1A] text-white' : 'bg-white border border-gray-300 text-gray-700 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-300'}`}
                >
                  Rating
                </button>
                <button 
                  onClick={() => setSortBy('sales')}
                  className={`whitespace-nowrap px-4 py-1.5 rounded-full text-[11px] font-bold transition-all shadow-sm ${sortBy === 'sales' ? 'bg-[#FF1A1A] text-white' : 'bg-white border border-gray-300 text-gray-700 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-300'}`}
                >
                  Most Reviewed
                </button>
              </div>
            </div>

            {/* Merchants Cards scroll container */}
            <div className="px-4 pb-20 space-y-3 shrink-0">
              {filteredMerchants.length > 0 ? (
                filteredMerchants.map((merchant) => (
                  <MerchantCard
                    key={merchant.id}
                    merchant={merchant}
                    isDarkMode={isDarkMode}
                    onClick={() => {
                      setHighlightedItemId(null);
                      setSelectedMerchant(merchant);
                      navigateToScreen('detail');
                    }}
                  />
                ))
              ) : (
                <div className="text-center py-16 flex flex-col items-center justify-center text-gray-400">
                  <Search size={32} className="opacity-40 mb-3" />
                  <p className="text-xs font-semibold">抱歉，连山没有搜索到匹配商户</p>
                  <p className="text-[10px] mt-1 opacity-70">试试输入 “汤糍” 或 “生鲜” 吧！</p>
                </div>
              )}
            </div>
          </motion.div>
        )}

        {currentScreen === 'detail' && selectedMerchant && (
          <motion.div
            key="detail"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <OrderDetailSheet
              merchant={selectedMerchant}
              isDarkMode={isDarkMode}
              onBack={() => {
                setSelectedMerchant(null);
                navigateToScreen('home');
              }}
              onCheckout={handleCheckoutInit}
              highlightItemId={highlightedItemId}
            />
          </motion.div>
        )}

        {currentScreen === 'orders' && (
          <motion.div
            key="orders"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <OrderListView
              isDarkMode={isDarkMode}
              onNavigateToHome={() => navigateToScreen('home')}
              onNavigateToTracker={(orderId) => {
                setActiveOrderId(orderId);
                navigateToScreen('tracker');
              }}
              onNavigateToDetail={(merchantId) => {
                const target = merchants.find(m => m.id === merchantId);
                if (target) {
                  setSelectedMerchant(target);
                  navigateToScreen('detail');
                }
              }}
            />
          </motion.div>
        )}

        {currentScreen === 'tracker' && activeOrderId && (
          <motion.div
            key="tracker"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <DeliveryTracker
              orderId={activeOrderId}
              isDarkMode={isDarkMode}
              onBackToHome={() => navigateToScreen('orders')}
            />
          </motion.div>
        )}

        {currentScreen === 'profile' && (
          <motion.div
            key="profile"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <UserProfile
              isDarkMode={isDarkMode}
              onBackToHome={() => navigateToScreen('home')}
              ordersCount={ordersCount}
              currentOrderAmount={currentOrderAmount}
              activeOrderId={activeOrderId}
              membershipTier={membershipTier}
              onShowSubscription={() => setIsSubscriptionModalOpen(true)}
            />
          </motion.div>
        )}

        {currentScreen === 'publish' && (
          <motion.div
            key="publish"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <PublishView
              isDarkMode={isDarkMode}
              membershipTier={membershipTier}
              publishedCount={publishedCount}
              onShowSubscription={() => setIsSubscriptionModalOpen(true)}
              onPublishSuccess={() => setPublishedCount(prev => prev + 1)}
            />
          </motion.div>
        )}

        {currentScreen === 'cart' && (
          <motion.div
            key="cart"
            custom={slideDirection}
            variants={screenVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="flex-1 flex flex-col min-h-0 w-full"
          >
            <CartView
              isDarkMode={isDarkMode}
              onNavigateToHome={() => navigateToScreen('home')}
              onSelectMerchant={(merchant) => {
                setHighlightedItemId(null);
                setSelectedMerchant(merchant);
                navigateToScreen('detail');
              }}
              merchants={merchants}
            />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Bottom Navigation Tab Bar for mobile layout */}
      {(currentScreen === 'home' || currentScreen === 'cart' || currentScreen === 'profile' || currentScreen === 'orders' || currentScreen === 'publish') && (
        <div className={`shrink-0 py-1.5 px-4 flex justify-around items-end z-30 transition-colors duration-300 pb-[calc(1rem+env(safe-area-inset-bottom))] shadow-[0_-8px_30px_rgba(0,0,0,0.04)] dark:shadow-[0_-8px_30px_rgba(0,0,0,0.2)] rounded-t-3xl ${
          isDarkMode 
            ? 'bg-gray-900 border-gray-800 text-white' 
            : 'bg-white border-gray-100 text-gray-800'
        }`}>
          {/* Home Tab */}
          <button
            onClick={() => navigateToScreen('home')}
            className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-300 ${
              currentScreen === 'home' 
                ? 'text-[#FF1A1A] scale-105' 
                : 'text-gray-400 hover:text-gray-500'
            }`}
          >
            <div className={`p-1 rounded-xl ${currentScreen === 'home' ? 'bg-red-50 dark:bg-[#FF1A1A]/10' : ''}`}>
              <Home size={20} strokeWidth={currentScreen === 'home' ? 3 : 2.5} className={currentScreen === 'home' ? 'text-[#FF1A1A] dark:text-[#FF1A1A]' : ''} />
            </div>
            <span className={`text-[10px] ${currentScreen === 'home' ? 'font-extrabold text-[#FF1A1A] dark:text-[#FF1A1A]' : 'font-medium'}`}>{t('nav.home')}</span>
          </button>

          {/* Orders tab */}
          <button
            onClick={() => navigateToScreen('orders')}
            className={`flex flex-col items-center gap-1 transition-all relative duration-300 ${
              currentScreen === 'orders'
                ? 'text-[#FF1A1A] scale-105 cursor-pointer'
                : 'text-gray-400 hover:text-gray-500 cursor-pointer'
            }`}
          >
            <div className={`p-1 rounded-xl ${currentScreen === 'orders' ? 'bg-red-50 dark:bg-[#FF1A1A]/10' : ''}`}>
              <FileText size={20} strokeWidth={currentScreen === 'orders' ? 3 : 2.5} className={currentScreen === 'orders' ? 'text-[#FF1A1A] dark:text-[#FF1A1A]' : ''} />
            </div>
            <span className={`text-[10px] ${currentScreen === 'orders' ? 'font-extrabold text-[#FF1A1A] dark:text-[#FF1A1A]' : 'font-medium'}`}>{t('nav.activity')}</span>
            {activeOrderId && (
              <span className="absolute top-0 right-1 flex h-1.5 w-1.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-pink-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-pink-500"></span>
              </span>
            )}
          </button>

          {/* Core Publish Button */}
          <button
            onClick={() => currentScreen === 'publish' ? navigateToScreen('home') : navigateToScreen('publish')}
            className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-300 ${
              currentScreen === 'publish' 
                ? 'text-[#FF1A1A] scale-105' 
                : 'text-gray-400 hover:text-gray-500'
            }`}
          >
            <div className={`p-1 rounded-xl ${currentScreen === 'publish' ? 'bg-red-50 dark:bg-[#FF1A1A]/10' : ''}`}>
              <PlusCircle size={20} strokeWidth={currentScreen === 'publish' ? 3 : 2.5} className={currentScreen === 'publish' ? 'text-[#FF1A1A] dark:text-[#FF1A1A]' : ''} />
            </div>
            <span className={`text-[10px] ${currentScreen === 'publish' ? 'font-extrabold text-[#FF1A1A] dark:text-[#FF1A1A]' : 'font-medium'}`}>{t('nav.publish')}</span>
          </button>

          {/* Shopping Cart Tab */}
          <button
            onClick={() => navigateToScreen('cart')}
            className={`flex flex-col items-center gap-1 cursor-pointer transition-all relative duration-300 ${
              currentScreen === 'cart' 
                ? 'text-[#FF1A1A] scale-105' 
                : 'text-gray-400 hover:text-gray-500'
            }`}
          >
            <div className={`p-1 rounded-xl ${currentScreen === 'cart' ? 'bg-red-50 dark:bg-[#FF1A1A]/10' : ''}`}>
              <ShoppingCart size={20} strokeWidth={currentScreen === 'cart' ? 3 : 2.5} className={currentScreen === 'cart' ? 'text-[#FF1A1A] dark:text-[#FF1A1A]' : ''} />
            </div>
            <span className={`text-[10px] ${currentScreen === 'cart' ? 'font-extrabold text-[#FF1A1A] dark:text-[#FF1A1A]' : 'font-medium'}`}>{t('nav.cart')}</span>
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-2 bg-rose-500 text-white text-[9px] font-black h-4 w-4 rounded-full flex items-center justify-center ring-2 ring-white dark:ring-gray-900 shadow-md">
                {cartCount}
              </span>
            )}
          </button>

          {/* Profile Tab */}
          <button
            onClick={() => navigateToScreen('profile')}
            className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-300 ${
              currentScreen === 'profile' 
                ? 'text-[#FF1A1A] scale-105' 
                : 'text-gray-400 hover:text-gray-500'
            }`}
          >
            <div className={`p-1 rounded-xl ${currentScreen === 'profile' ? 'bg-red-50 dark:bg-[#FF1A1A]/10' : ''}`}>
              <User size={20} strokeWidth={currentScreen === 'profile' ? 3 : 2.5} className={currentScreen === 'profile' ? 'text-[#FF1A1A] dark:text-[#FF1A1A]' : ''} />
            </div>
            <span className={`text-[10px] ${currentScreen === 'profile' ? 'font-extrabold text-[#FF1A1A] dark:text-[#FF1A1A]' : 'font-medium'}`}>{t('nav.me')}</span>
          </button>
        </div>
      )}

        {/* Global secure payment收银台 modal */}
        {pendingOrderDetails && (
          <PaymentModal
            isOpen={isPayOpen}
            orderId={'LS' + Math.floor(Math.random() * 900000 + 100000)}
            amount={pendingOrderDetails.amount}
            merchantName={pendingOrderDetails.merchantName}
            isDarkMode={isDarkMode}
            onClose={() => setIsPayOpen(false)}
            onPaymentSuccess={handlePaymentCompleted}
          />
        )}
        
        <SubscriptionModal 
          isOpen={isSubscriptionModalOpen}
          onClose={() => setIsSubscriptionModalOpen(false)}
          currentTier={membershipTier}
          onUpgrade={(tier) => {
            setMembershipTier(tier);
          }}
        />
      </div>
    </div>
  );
}
