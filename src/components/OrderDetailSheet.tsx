/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ArrowLeft, 
  Search, 
  Star, ShoppingCart, 
  Plus, 
  Minus, 
  Trash2, 
  Bike, 
  MapPin, 
  Clock, 
  ChevronRight, 
  CheckCircle2, 
  X,
  CreditCard,
  MessageCircle,
  Send
} from 'lucide-react';
import { Merchant, MenuItem, CartItem } from '../types';

interface OrderDetailSheetProps {
  merchant: Merchant;
  isDarkMode?: boolean;
  onBack: () => void;
  onCheckout: (cart: CartItem[], paymentMethod: 'alipay' | 'wechat' | 'wallet', deliveryAddress: { name: string, phone: string, address: string }) => void;
  highlightItemId?: string | null;
}

export default function OrderDetailSheet({
  merchant,
  isDarkMode = false,
  onBack,
  onCheckout,
  highlightItemId,
}: OrderDetailSheetProps) {
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [cart, setCart] = useState<CartItem[]>(() => {
    try {
      const saved = localStorage.getItem('user_carts');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed[merchant.id]) {
          return parsed[merchant.id].items || [];
        }
      }
    } catch (e) {
      console.error(e);
    }
    return [];
  });
  const [isCartExpanded, setIsCartExpanded] = useState<boolean>(false);
  const [checkoutStep, setCheckoutStep] = useState<'shopping' | 'confirming'>('shopping');

  // Delivery Address form state (loaded from UserProfile's addresses)
  const [deliveryAddress, setDeliveryAddress] = useState(() => {
    try {
      const saved = localStorage.getItem('user_delivery_addresses');
      if (saved) {
        const list = JSON.parse(saved);
        const def = list.find((a: any) => a.isDefault);
        if (def) {
          return {
            name: def.name,
            phone: def.phone,
            address: def.address
          };
        } else if (list.length > 0) {
          return {
            name: list[0].name,
            phone: list[0].phone,
            address: list[0].address
          };
        }
      }
    } catch (e) {
      console.error(e);
    }
    return {
      name: '徐先生',
      phone: '138-2512-9988',
      address: '连山壮族瑶族自治县 瑶香苑小区 3栋101室'
    };
  });
  const [selectedPayment, setSelectedPayment] = useState<'alipay' | 'wechat' | 'wallet'>('alipay');
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState<{sender: 'user'|'merchant', text: string, time: string}[]>([
    { sender: 'merchant', text: `Hello! Welcome to ${merchant.name}. How can we help you today?`, time: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }
  ]);
  const [chatInput, setChatInput] = useState('');

  // Sync cart with localStorage's user_carts
  useEffect(() => {
    try {
      const savedStr = localStorage.getItem('user_carts');
      const saved = savedStr ? JSON.parse(savedStr) : {};
      
      if (cart.length === 0) {
        delete saved[merchant.id];
      } else {
        saved[merchant.id] = {
          merchantId: merchant.id,
          merchantName: merchant.name,
          merchantLogo: merchant.logo,
          deliveryFee: merchant.deliveryFee,
          items: cart
        };
      }
      
      localStorage.setItem('user_carts', JSON.stringify(saved));
      window.dispatchEvent(new Event('storage'));
    } catch (e) {
      console.error('Failed to sync cart:', e);
    }
  }, [cart, merchant]);

  // Group food items by category
  const categories = useMemo(() => {
    const list = Array.from(new Set(merchant.items.map((i) => i.category)));
    return list;
  }, [merchant]);

  useEffect(() => {
    if (categories.length > 0) {
      setSelectedCategory(categories[0]);
    }
  }, [categories]);

  // Handle highlighted item (AI trigger auto add-to-cart or scroll highlight!)
  useEffect(() => {
    if (highlightItemId) {
      const itemToHighlight = merchant.items.find(i => i.id === highlightItemId);
      if (itemToHighlight) {
        // Auto add to cart if not already present
        setCart(prev => {
          const existing = prev.find(c => c.item.id === highlightItemId);
          if (existing) return prev;
          return [...prev, { item: itemToHighlight, quantity: 1 }];
        });
        
        // Find item's category and select it
        setSelectedCategory(itemToHighlight.category);

        // Flash message
        alert(`已为您自动定位并加购: "${itemToHighlight.name}"`);
      }
    }
  }, [highlightItemId, merchant]);

  const handleAddToCart = (item: MenuItem) => {
    setCart((prev) => {
      const existing = prev.find((c) => c.item.id === item.id);
      if (existing) {
        return prev.map((c) =>
          c.item.id === item.id ? { ...c, quantity: c.quantity + 1 } : c
        );
      }
      return [...prev, { item, quantity: 1 }];
    });
  };

  const handleRemoveFromCart = (item: MenuItem) => {
    setCart((prev) => {
      const existing = prev.find((c) => c.item.id === item.id);
      if (!existing) return prev;
      if (existing.quantity === 1) {
        return prev.filter((c) => c.item.id !== item.id);
      }
      return prev.map((c) =>
        c.item.id === item.id ? { ...c, quantity: c.quantity - 1 } : c
      );
    });
  };

  const clearCart = () => {
    setCart([]);
    setIsCartExpanded(false);
  };

  const cartSummary = useMemo(() => {
    let totalItems = 0;
    let totalPrice = 0;
    cart.forEach((c) => {
      totalItems += c.quantity;
      totalPrice += c.item.price * c.quantity;
    });
    return { totalItems, totalPrice };
  }, [cart]);

  const finalTotal = cartSummary.totalPrice + merchant.deliveryFee;

  const handleSendMessage = () => {
    if (!chatInput.trim()) return;
    const newMessage = { sender: 'user' as const, text: chatInput.trim(), time: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) };
    setChatMessages(prev => [...prev, newMessage]);
    setChatInput('');
    
    // Auto reply
    setTimeout(() => {
      setChatMessages(prev => [...prev, {
        sender: 'merchant',
        text: 'We are currently busy, but we will get back to you as soon as possible!',
        time: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
      }]);
    }, 1500);
  };

  return (
    <div className="flex flex-col h-full bg-gray-50 dark:bg-black transition-colors duration-300">
      
      {/* Merchant Header Bar */}
      <div 
        className="relative shrink-0 h-44 flex flex-col justify-between p-4 bg-cover bg-center"
        style={{ backgroundImage: `linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.6)), url(${merchant.banner})` }}
      >
        <div className="flex items-center justify-between">
          <button 
            onClick={onBack}
            className="p-2 rounded-full bg-black/40 text-white backdrop-blur-sm hover:bg-black/60 transition cursor-pointer"
          >
            <ArrowLeft size={18} />
          </button>
          <div className="flex gap-2">
            <button 
              onClick={() => setIsChatOpen(true)}
              className="flex items-center gap-1 bg-white/20 hover:bg-white/30 backdrop-blur-md text-white px-3 py-1 rounded-full text-xs font-bold transition-all cursor-pointer shadow-sm"
            >
              <MessageCircle size={14} />
              Message
            </button>
            <span className="text-xs font-bold bg-green-500 text-white px-2.5 py-1 rounded-full flex items-center justify-center">
              Open
            </span>
          </div>
        </div>

        {/* Merchant Quick stats overlay */}
        <div className="text-white mt-auto">
          <h2 className="text-[17px] font-black tracking-tight drop-shadow-md">{merchant.name}</h2>
          
          <div className="flex items-center gap-1.5 mt-1.5">
            <div className="flex bg-[#FF1A1A] p-1 rounded-sm shadow-sm">
              {[...Array(5)].map((_, i) => (
                <Star 
                  key={i} 
                  size={14} 
                  className={i < Math.round(merchant.rating) ? 'text-white fill-white' : 'text-white/30 fill-white/30'} 
                />
              ))}
            </div>
            <span className="text-[14px] font-bold text-white drop-shadow-sm ml-1">
              {Math.floor(merchant.sales * 1.5)} reviews
            </span>
          </div>
          
          <div className="flex items-center gap-2 mt-1.5 text-[13px] font-semibold text-gray-100 drop-shadow-sm">
            <span>{merchant.avgPrice > 50 ? '$$$' : merchant.avgPrice > 20 ? '$$' : '$'}</span>
            <span>•</span>
            <span>{merchant.category === 'food' ? 'Restaurants' : 'Local Services'}</span>
            <span>•</span>
            <span className="text-green-400 font-bold">Open</span>
            <span className="font-normal opacity-90 text-[11px] ml-1">until 10:00 PM</span>
          </div>
        </div>
      </div>

      {checkoutStep === 'shopping' ? (
        /* SHOPPING INTERFACE */
        <div className="flex-1 overflow-hidden flex min-h-0">
          
          {/* Left: Sidelist categories */}
          <div className="w-24 shrink-0 bg-white dark:bg-gray-950 border-r border-gray-100 dark:border-gray-800 overflow-y-auto">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`w-full text-left py-4 px-3 text-xs font-bold border-l-4 transition cursor-pointer truncate ${
                  selectedCategory === cat
                    ? 'border-red-500 bg-red-500/5 dark:bg-red-500/10 text-red-500'
                    : 'border-transparent text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-900/50'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Right: Items detail list */}
          <div className="flex-1 bg-white dark:bg-gray-900 overflow-y-auto p-4 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-gray-50 dark:border-gray-800/50">
              <span className="font-extrabold text-xs text-gray-400 uppercase tracking-wider">{selectedCategory}</span>
              <span className="text-[10px] text-gray-400 font-medium">手工鲜制美味</span>
            </div>

            {merchant.items
              .filter((item) => item.category === selectedCategory)
              .map((item) => {
                const cartQty = cart.find((c) => c.item.id === item.id)?.quantity || 0;
                const isItemHighlighted = highlightItemId === item.id;

                return (
                  <div
                    key={item.id}
                    className={`flex gap-3 pb-4 border-b border-gray-50 dark:border-gray-800/40 last:border-0 transition-all rounded-xl p-2 ${
                      isItemHighlighted ? 'bg-red-500/5 ring-1 ring-red-500/35' : ''
                    }`}
                  >
                    {/* Item Image */}
                    <img
                      src={item.image}
                      alt={item.name}
                      referrerPolicy="no-referrer"
                      className="w-20 h-20 rounded-xl object-cover shrink-0 bg-gray-100 dark:bg-gray-800"
                    />

                    {/* Details */}
                    <div className="flex-1 min-w-0 flex flex-col justify-between">
                      <div>
                        <h4 className="font-bold text-xs text-gray-900 dark:text-white truncate">
                          {item.name}
                        </h4>
                        <p className="text-[10px] text-gray-400 dark:text-gray-400 mt-1 line-clamp-1">
                          {item.desc}
                        </p>
                        <p className="text-[9px] text-gray-400 mt-1 font-medium">
                          月售 {item.sales}+ · 好评 98%
                        </p>
                      </div>

                      {/* Pricing and Action */}
                      <div className="flex items-center justify-between mt-2">
                        <div className="flex items-baseline gap-1.5">
                          <span className="text-xs font-extrabold text-red-500 dark:text-red-400">¥{item.price}</span>
                          {item.originalPrice && (
                            <span className="text-[10px] text-gray-400 line-through">¥{item.originalPrice}</span>
                          )}
                        </div>

                        {/* Cart controls */}
                        <div className="flex items-center gap-2">
                          <AnimatePresence>
                            {cartQty > 0 && (
                              <>
                                <motion.button
                                  initial={{ scale: 0.8, opacity: 0 }}
                                  animate={{ scale: 1, opacity: 1 }}
                                  exit={{ scale: 0.8, opacity: 0 }}
                                  onClick={() => handleRemoveFromCart(item)}
                                  className="p-1.5 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-750 text-gray-700 dark:text-gray-200 cursor-pointer"
                                >
                                  <Minus size={12} />
                                </motion.button>
                                <span className="text-xs font-bold w-4 text-center font-mono">
                                  {cartQty}
                                </span>
                              </>
                            )}
                          </AnimatePresence>
                          <button
                            onClick={() => handleAddToCart(item)}
                            className="p-1.5 rounded-full bg-red-500 text-white hover:bg-red-600 cursor-pointer transition shadow shadow-blue-500/20"
                          >
                            <Plus size={12} />
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
          </div>
        </div>
      ) : (
        /* CHECKOUT CONFIRMATION VIEW */
        <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 dark:bg-black">
          
          {/* Header step back */}
          <button
            onClick={() => setCheckoutStep('shopping')}
            className="flex items-center gap-1 text-xs text-red-500 font-bold hover:underline"
          >
            <ArrowLeft size={12} />
            <span>修改所购菜品</span>
          </button>

          {/* Delivery Address Card */}
          <div className="p-4 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-xs space-y-3">
            <div className="flex justify-between items-center pb-2 border-b border-gray-50 dark:border-gray-800/40">
              <span className="text-xs font-extrabold text-gray-400 uppercase tracking-wider">配送信息</span>
              <span className="text-[10px] text-green-500 font-bold">美团专送 · 极速达</span>
            </div>

            <div className="space-y-2">
              <div className="flex items-start gap-2.5 text-xs text-gray-700 dark:text-gray-300">
                <MapPin size={14} className="text-red-500 shrink-0 mt-0.5" />
                <div>
                  <input
                    type="text"
                    value={deliveryAddress.address}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, address: e.target.value })}
                    className="w-full bg-transparent border-b border-transparent focus:border-red-500 outline-none pb-0.5 text-xs font-bold"
                  />
                  <div className="flex gap-2 text-[11px] text-gray-400 mt-1 font-medium">
                    <input
                      type="text"
                      value={deliveryAddress.name}
                      onChange={(e) => setDeliveryAddress({ ...deliveryAddress, name: e.target.value })}
                      className="bg-transparent outline-none max-w-[60px]"
                    />
                    <span>·</span>
                    <input
                      type="text"
                      value={deliveryAddress.phone}
                      onChange={(e) => setDeliveryAddress({ ...deliveryAddress, phone: e.target.value })}
                      className="bg-transparent outline-none max-w-[100px]"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Order items List Card */}
          <div className="p-4 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-xs space-y-3">
            <p className="text-xs font-extrabold text-gray-400 uppercase tracking-wider pb-1 border-b border-gray-50 dark:border-gray-800/40">
              购买明细
            </p>
            
            <div className="space-y-3">
              {cart.map((c) => (
                <div key={c.item.id} className="flex justify-between items-center text-xs">
                  <div className="flex items-center gap-2">
                    <img src={c.item.image} alt={c.item.name} referrerPolicy="no-referrer" className="w-8 h-8 rounded-lg object-cover" />
                    <div>
                      <h5 className="font-bold">{c.item.name}</h5>
                      <span className="text-[10px] text-gray-400 font-medium">单价: ¥{c.item.price}</span>
                    </div>
                  </div>
                  <span className="font-bold text-gray-600 dark:text-gray-300">
                    x{c.quantity} · ¥{(c.item.price * c.quantity).toFixed(1)}
                  </span>
                </div>
              ))}
            </div>

            <div className="pt-2 border-t border-gray-50 dark:border-gray-800/40 space-y-1 text-xs">
              <div className="flex justify-between text-gray-500">
                <span>商品小计</span>
                <span>¥{cartSummary.totalPrice.toFixed(1)}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>配送服务费</span>
                <span>¥{merchant.deliveryFee.toFixed(1)}</span>
              </div>
              <div className="flex justify-between font-extrabold text-sm pt-2">
                <span>待支付总额</span>
                <span className="text-red-500 dark:text-red-400">¥{finalTotal.toFixed(1)}</span>
              </div>
            </div>
          </div>

          {/* Checkout Payment Gateway selection */}
          <div className="p-4 rounded-2xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-xs space-y-2">
            <p className="text-xs font-extrabold text-gray-400 uppercase tracking-wider pb-1">选择默认扣款账户</p>
            
            <div className="grid grid-cols-3 gap-2">
              <button
                onClick={() => setSelectedPayment('alipay')}
                className={`py-2 px-3 rounded-xl border text-center font-bold text-xs flex flex-col items-center justify-center gap-1.5 transition ${
                  selectedPayment === 'alipay' 
                    ? 'border-red-500 bg-red-500/5 text-red-500' 
                    : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 text-gray-500'
                }`}
              >
                <div className="w-6 h-6 rounded-md bg-red-500 text-white flex items-center justify-center font-black text-xs">支</div>
                <span>支付宝</span>
              </button>

              <button
                onClick={() => setSelectedPayment('wechat')}
                className={`py-2 px-3 rounded-xl border text-center font-bold text-xs flex flex-col items-center justify-center gap-1.5 transition ${
                  selectedPayment === 'wechat' 
                    ? 'border-green-500 bg-green-500/5 text-green-500' 
                    : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 text-gray-500'
                }`}
              >
                <div className="w-6 h-6 rounded-md bg-green-500 text-white flex items-center justify-center font-black text-xs">微</div>
                <span>微信支付</span>
              </button>

              <button
                onClick={() => setSelectedPayment('wallet')}
                className={`py-2 px-3 rounded-xl border text-center font-bold text-xs flex flex-col items-center justify-center gap-1.5 transition ${
                  selectedPayment === 'wallet' 
                    ? 'border-amber-500 bg-amber-500/5 text-amber-500' 
                    : 'border-gray-100 dark:border-gray-800 hover:bg-gray-50 text-gray-500'
                }`}
              >
                <CreditCard size={16} className={selectedPayment === 'wallet' ? 'text-amber-500' : 'text-gray-400'} />
                <span>零钱余额</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Floating Cart bar / Checkout Action Footer */}
      <div className="shrink-0 bg-white dark:bg-gray-950 border-t border-gray-100 dark:border-gray-800 p-4">
        {checkoutStep === 'shopping' ? (
          /* Shopping Cart Bar */
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button
                onClick={() => cartSummary.totalItems > 0 && setIsCartExpanded(!isCartExpanded)}
                className={`p-3 rounded-full relative shadow transition duration-200 cursor-pointer ${
                  cartSummary.totalItems > 0 
                    ? 'bg-red-500 text-white' 
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-400'
                }`}
              >
                <ShoppingCart size={20} />
                {cartSummary.totalItems > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[9px] font-bold h-5 w-5 rounded-full flex items-center justify-center ring-2 ring-white dark:ring-gray-950">
                    {cartSummary.totalItems}
                  </span>
                )}
              </button>

              <div>
                <p className="text-sm font-black">
                  {cartSummary.totalItems > 0 ? `¥${cartSummary.totalPrice.toFixed(1)}` : '未选择项目'}
                </p>
                <p className="text-[10px] text-gray-400 mt-0.5">
                  {merchant.isFood 
                    ? `同城配送 · 起送费 ¥0 · 配送 ¥${merchant.deliveryFee}`
                    : merchant.category === 'jobs' || merchant.category === 'housing'
                      ? '直接联系 / 在线沟通'
                      : '上门服务 / 预约免配送费'
                  }
                </p>
              </div>
            </div>

            <button
              disabled={cartSummary.totalItems === 0}
              onClick={() => setCheckoutStep('confirming')}
              className={`px-8 py-3 rounded-xl font-bold text-xs tracking-wider transition ${
                cartSummary.totalItems > 0
                  ? 'bg-red-500 hover:bg-red-600 text-white shadow shadow-blue-500/10 cursor-pointer'
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-400 cursor-not-allowed'
              }`}
            >
              {merchant.isFood ? '去结算' : '立即预约/投递'}
            </button>
          </div>
        ) : (
          /* Checkout Confirm Bar */
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-400">实付总额</p>
              <p className="text-lg font-black text-red-500 dark:text-red-400">¥{finalTotal.toFixed(1)}</p>
            </div>

            <button
              onClick={() => onCheckout(cart, selectedPayment, deliveryAddress)}
              className="px-10 py-3.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-bold text-sm tracking-wide shadow-lg shadow-blue-500/15 cursor-pointer"
            >
              {merchant.isFood ? '提交订单并支付' : '确认提交'}
            </button>
          </div>
        )}
      </div>

      {/* Expanded Mini Cart Overlay */}
      <AnimatePresence>
        {isCartExpanded && cartSummary.totalItems > 0 && (
          <div className="fixed inset-0 z-40 flex items-end">
            <div 
              className="absolute inset-0 bg-black/50 backdrop-blur-xs" 
              onClick={() => setIsCartExpanded(false)}
            />
            
            <motion.div
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              className={`relative w-full max-h-[75vh] flex flex-col rounded-t-3xl shadow-2xl overflow-hidden transition-colors ${
                isDarkMode ? 'bg-gray-950 text-white' : 'bg-white text-gray-950'
              }`}
            >
              {/* Header */}
              <div className="flex items-center justify-between p-4 border-b border-gray-100 dark:border-gray-850 shrink-0">
                <span className="font-extrabold text-sm flex items-center gap-1.5">
                  <ShoppingCart size={16} className="text-red-500" />
                  已选服务项目
                </span>
                <button
                  onClick={clearCart}
                  className="text-xs font-bold text-red-500 flex items-center gap-1 hover:underline cursor-pointer"
                >
                  <Trash2 size={13} />
                  <span>清空列表</span>
                </button>
              </div>

              {/* Items scroll list */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                {cart.map((c) => (
                  <div key={c.item.id} className="flex items-center justify-between">
                    <div>
                      <h4 className="text-xs font-bold">{c.item.name}</h4>
                      <p className="text-[10px] text-red-500 font-bold mt-0.5">¥{c.item.price}</p>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleRemoveFromCart(c.item)}
                        className="p-1 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-200"
                      >
                        <Minus size={11} />
                      </button>
                      <span className="text-xs font-bold w-4 text-center font-mono">{c.quantity}</span>
                      <button
                        onClick={() => handleAddToCart(c.item)}
                        className="p-1 rounded-full bg-red-500 text-white"
                      >
                        <Plus size={11} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Action Button spacer */}
              <div className="p-4 border-t border-gray-100 dark:border-gray-850 bg-gray-50/50 dark:bg-gray-900/20 shrink-0">
                <button
                  onClick={() => {
                    setIsCartExpanded(false);
                    setCheckoutStep('confirming');
                  }}
                  className="w-full py-3 rounded-xl bg-red-500 text-white font-bold text-sm shadow hover:bg-red-600 transition"
                >
                  {merchant.isFood ? '确定加购并去结算' : '确认选择并去预约'}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Chat Overlay Modal */}
      <AnimatePresence>
        {isChatOpen && (
          <div className="fixed inset-0 z-[100] flex flex-col justify-end p-0 sm:p-4">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsChatOpen(false)}
              className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            />
            <motion.div 
              initial={{ opacity: 0, y: 100 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 100 }}
              className="relative w-full h-[85vh] sm:h-[600px] sm:max-w-[430px] mx-auto bg-white dark:bg-gray-950 sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl flex flex-col overflow-hidden"
            >
              {/* Chat Header */}
              <div className="px-5 py-4 border-b border-gray-100 dark:border-gray-800 flex items-center justify-between bg-white/80 dark:bg-gray-950/80 backdrop-blur-md sticky top-0 z-10">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 overflow-hidden shrink-0">
                    <img src={merchant.logo} alt={merchant.name} className="w-full h-full object-cover" />
                  </div>
                  <div>
                    <h3 className="font-extrabold text-gray-900 dark:text-white text-[15px]">{merchant.name}</h3>
                    <p className="text-[11px] font-medium text-green-500">在线 · Open</p>
                  </div>
                </div>
                <button 
                  onClick={() => setIsChatOpen(false)}
                  className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 transition"
                >
                  <X size={18} strokeWidth={2.5} />
                </button>
              </div>

              {/* Chat Messages */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 dark:bg-gray-900/50">
                {chatMessages.map((msg, idx) => (
                  <div key={idx} className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-[75%] rounded-2xl px-4 py-3 ${
                      msg.sender === 'user' 
                        ? 'bg-red-500 text-white rounded-br-sm' 
                        : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 rounded-bl-sm border border-gray-100 dark:border-gray-700 shadow-sm'
                    }`}>
                      <p className="text-[14px] leading-relaxed break-words">{msg.text}</p>
                      <p className={`text-[9px] mt-1 text-right ${msg.sender === 'user' ? 'text-red-100' : 'text-gray-400'}`}>
                        {msg.time}
                      </p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Chat Input */}
              <div className="p-4 bg-white dark:bg-gray-950 border-t border-gray-100 dark:border-gray-800 flex items-center gap-2">
                <input
                  type="text"
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
                  placeholder="Message this business..."
                  className="flex-1 bg-gray-50 dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-full px-5 py-3 text-[14px] text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500"
                />
                <button
                  onClick={handleSendMessage}
                  disabled={!chatInput.trim()}
                  className={`w-12 h-12 rounded-full flex items-center justify-center transition-all ${
                    chatInput.trim() 
                      ? 'bg-red-500 text-white hover:bg-red-600 shadow-md active:scale-95' 
                      : 'bg-gray-100 dark:bg-gray-800 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  <Send size={18} strokeWidth={2.5} className={chatInput.trim() ? '-ml-0.5' : ''} />
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
