import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ShoppingCart, 
  Trash2, 
  Plus, 
  Minus, 
  Store, 
  ChevronRight, 
  MapPin, 
  CreditCard,
  CheckCircle2,
  ArrowRight
} from 'lucide-react';
import { CartItem, Merchant } from '../types';

interface CartViewProps {
  isDarkMode: boolean;
  onNavigateToHome: () => void;
  onSelectMerchant: (merchant: Merchant) => void;
  merchants: Merchant[];
}

interface SavedCart {
  merchantId: string;
  merchantName: string;
  merchantLogo: string;
  deliveryFee: number;
  items: CartItem[];
}

export default function CartView({
  isDarkMode,
  onNavigateToHome,
  onSelectMerchant,
  merchants,
}: CartViewProps) {
  const [carts, setCarts] = useState<{ [key: string]: SavedCart }>({});

  // Load carts from localStorage
  const loadCarts = () => {
    try {
      const saved = localStorage.getItem('user_carts');
      if (saved) {
        setCarts(JSON.parse(saved));
      } else {
        setCarts({});
      }
    } catch (e) {
      console.error('Failed to load carts:', e);
    }
  };

  useEffect(() => {
    loadCarts();
    // Watch for storage changes
    const handleStorageChange = () => {
      loadCarts();
    };
    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  // Update localStorage and state
  const saveCarts = (newCarts: { [key: string]: SavedCart }) => {
    try {
      // Remove empty merchant carts
      const cleaned = { ...newCarts };
      Object.keys(cleaned).forEach((merchantId) => {
        if (!cleaned[merchantId] || cleaned[merchantId].items.length === 0) {
          delete cleaned[merchantId];
        }
      });

      localStorage.setItem('user_carts', JSON.stringify(cleaned));
      setCarts(cleaned);
      // Trigger update for other parts of the app
      window.dispatchEvent(new Event('storage'));
    } catch (e) {
      console.error('Failed to save carts:', e);
    }
  };

  const handleUpdateQuantity = (merchantId: string, itemId: string, delta: number) => {
    const targetCart = carts[merchantId];
    if (!targetCart) return;

    const updatedItems = targetCart.items.map((cartItem) => {
      if (cartItem.item.id === itemId) {
        const newQty = cartItem.quantity + delta;
        return { ...cartItem, quantity: Math.max(0, newQty) };
      }
      return cartItem;
    }).filter((cartItem) => cartItem.quantity > 0);

    const newCarts = { ...carts };
    if (updatedItems.length === 0) {
      delete newCarts[merchantId];
    } else {
      newCarts[merchantId] = {
        ...targetCart,
        items: updatedItems,
      };
    }

    saveCarts(newCarts);
  };

  const handleClearMerchantCart = (merchantId: string) => {
    const confirmClear = window.confirm('确定要清空该商家的购物车吗？');
    if (!confirmClear) return;
    
    const newCarts = { ...carts };
    delete newCarts[merchantId];
    saveCarts(newCarts);
  };

  const handleCheckoutMerchant = (merchantId: string) => {
    const merchant = merchants.find((m) => m.id === merchantId);
    if (merchant) {
      onSelectMerchant(merchant);
    } else {
      alert('未找到该商家的详细信息');
    }
  };

  // Convert carts object to array
  const cartList = useMemo(() => {
    return Object.values(carts);
  }, [carts]);

  // Total amount across all carts
  const globalTotal = useMemo(() => {
    return cartList.reduce((sum, cart) => {
      const itemsSubtotal = cart.items.reduce((itemSum, item) => itemSum + item.item.price * item.quantity, 0);
      return sum + itemsSubtotal + cart.deliveryFee;
    }, 0);
  }, [cartList]);

  // Total quantity of items in all carts
  const globalItemCount = useMemo(() => {
    return cartList.reduce((sum, cart) => {
      return sum + cart.items.reduce((itemSum, item) => itemSum + item.quantity, 0);
    }, 0);
  }, [cartList]);

  return (
    <div className={`relative flex flex-col h-full overflow-hidden transition-colors duration-300 ${
      isDarkMode ? 'bg-black text-gray-100' : 'bg-gray-50 text-gray-900'
    }`}>
      {/* Header */}
      <div className="p-3 pb-4 pt-5 bg-gradient-to-br from-red-500/95 to-indigo-600/95 text-white shrink-0 backdrop-blur-md shadow-sm relative z-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-1.5 bg-white/10 rounded-xl">
              <ShoppingCart size={18} className="text-white animate-pulse" />
            </div>
            <div>
              <h1 className="text-sm font-black tracking-wider uppercase">我的购物车</h1>
              <p className="text-[10px] text-red-100 font-semibold">
                {globalItemCount > 0 ? `共加购了 ${globalItemCount} 件商品` : '挑选连山当地好物美味'}
              </p>
            </div>
          </div>
          {globalItemCount > 0 && (
            <span className="px-2 py-0.5 text-[10px] font-black rounded-lg bg-white/20 text-white font-mono">
              ¥{globalTotal.toFixed(1)}
            </span>
          )}
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto p-3 space-y-4 pb-24">
        {cartList.length === 0 ? (
          /* Empty State */
          <div className="py-24 mt-8 text-center flex flex-col items-center justify-center bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-[2rem] p-4 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)]">
            <div className="w-20 h-20 rounded-[20px] bg-gradient-to-br from-red-50 to-indigo-50 dark:from-red-500/10 dark:to-indigo-500/10 flex items-center justify-center mb-5 text-red-500">
              <ShoppingCart size={32} strokeWidth={2.5} />
            </div>
            <p className="text-[15px] font-black tracking-tight text-gray-900 dark:text-white">您的购物车空空如也</p>
            <p className="text-[10px] text-gray-400 dark:text-gray-500 mt-1 max-w-[200px]">
              还没加购任何商品呢，快去首页寻找连山的特色美味或优质商品吧！
            </p>
            <button
              onClick={onNavigateToHome}
              className="mt-6 px-8 py-3 rounded-2xl bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white text-[13px] font-bold shadow-[0_8px_20px_rgba(59,130,246,0.3)] transition active:scale-95 cursor-pointer flex items-center gap-1.5"
            >
              <span>去逛逛</span>
              <ArrowRight size={12} />
            </button>
          </div>
        ) : (
          /* List of Carts grouped by Merchant */
          <AnimatePresence>
            {cartList.map((cart) => {
              const itemsSubtotal = cart.items.reduce((sum, c) => sum + c.item.price * c.quantity, 0);
              const totalAmount = itemsSubtotal + cart.deliveryFee;

              return (
                <motion.div
                  key={cart.merchantId}
                  layout
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  transition={{ duration: 0.2 }}
                  className="bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-3xl overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)] flex flex-col"
                >
                  {/* Merchant Bar Header */}
                  <div className="px-4 py-3 bg-gray-50/50 dark:bg-gray-950/20 border-b border-gray-100 dark:border-gray-800/60 flex items-center justify-between">
                    <div 
                      onClick={() => handleCheckoutMerchant(cart.merchantId)}
                      className="flex items-center gap-2 cursor-pointer group flex-1"
                    >
                      <img 
                        src={cart.merchantLogo} 
                        alt={cart.merchantName} 
                        referrerPolicy="no-referrer"
                        className="w-6 h-6 rounded-lg object-cover shrink-0 border border-gray-100 dark:border-gray-800"
                      />
                      <span className="text-xs font-black text-gray-900 dark:text-white group-hover:text-red-500 transition-colors flex items-center gap-0.5">
                        {cart.merchantName}
                        <ChevronRight size={12} className="text-gray-400 group-hover:text-red-500 transition-colors" />
                      </span>
                    </div>

                    <button
                      onClick={() => handleClearMerchantCart(cart.merchantId)}
                      className="p-1 text-gray-400 hover:text-red-500 transition cursor-pointer"
                      title="清空当前购物车"
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>

                  {/* Items List */}
                  <div className="px-4 py-2 divide-y divide-gray-50 dark:divide-gray-800/40">
                    {cart.items.map((cartItem) => (
                      <div key={cartItem.item.id} className="py-3 first:pt-2 last:pb-2 flex items-center justify-between gap-3">
                        <div className="flex items-center gap-2.5 min-w-0 flex-1">
                          <img 
                            src={cartItem.item.image} 
                            alt={cartItem.item.name} 
                            referrerPolicy="no-referrer"
                            className="w-12 h-12 rounded-[10px] object-cover shrink-0 bg-gray-50 dark:bg-gray-800 border border-gray-100 dark:border-gray-800"
                          />
                          <div className="min-w-0">
                            <h4 className="text-[13px] font-extrabold text-gray-900 dark:text-white truncate">
                              {cartItem.item.name}
                            </h4>
                            <p className="text-[10px] text-gray-400 dark:text-gray-500 mt-0.5 font-semibold font-mono">
                              单价 ¥{cartItem.item.price}
                            </p>
                          </div>
                        </div>

                        {/* Quantity and subtotal */}
                        <div className="flex items-center gap-3 shrink-0">
                          <span className="text-xs font-extrabold text-gray-850 dark:text-gray-200 font-mono">
                            ¥{(cartItem.item.price * cartItem.quantity).toFixed(1)}
                          </span>

                          <div className="flex items-center gap-2 bg-gray-50 dark:bg-gray-900 px-2 py-1 rounded-xl border border-gray-100 dark:border-gray-800">
                            <button
                              onClick={() => handleUpdateQuantity(cart.merchantId, cartItem.item.id, -1)}
                              className="p-0.5 rounded-full hover:bg-gray-200 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-300 transition cursor-pointer"
                            >
                              <Minus size={10} />
                            </button>
                            <span className="text-[13px] font-black w-5 text-center font-mono text-gray-900 dark:text-white">
                              {cartItem.quantity}
                            </span>
                            <button
                              onClick={() => handleUpdateQuantity(cart.merchantId, cartItem.item.id, 1)}
                              className="p-0.5 rounded-full hover:bg-gray-200 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-300 transition cursor-pointer"
                            >
                              <Plus size={10} />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Merchant subtotal & Go to Settle Button */}
                  <div className="px-4 py-3.5 bg-gray-50/50 dark:bg-gray-950/20 border-t border-gray-100 dark:border-gray-800/60 flex items-center justify-between text-xs font-bold">
                    <div className="text-[10px] text-gray-400 dark:text-gray-500 font-semibold">
                      <span>商品: ¥{itemsSubtotal.toFixed(1)} · 配送费: ¥{cart.deliveryFee.toFixed(1)}</span>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="text-[14px] font-black text-gray-900 dark:text-white font-mono">
                        小计: ¥{totalAmount.toFixed(1)}
                      </span>
                      <button
                        onClick={() => handleCheckoutMerchant(cart.merchantId)}
                        className="px-5 py-2 rounded-[10px] bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white font-bold text-[12px] shadow-[0_4px_10px_rgba(59,130,246,0.3)] transition active:scale-95 cursor-pointer flex items-center gap-1"
                      >
                        <span>去结算</span>
                        <ChevronRight size={11} />
                      </button>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        )}
      </div>

      {/* Static bottom layout info to complete the design */}
      <div className="absolute bottom-0 inset-x-0 py-4 text-center text-[10px] text-gray-450 dark:text-gray-500 font-bold tracking-wide pointer-events-none uppercase bg-gradient-to-t from-gray-50 dark:from-black to-transparent">
        © 连山壮族瑶族自治县 · 智慧同城生活平台
      </div>
    </div>
  );
}
