import React, { createContext, useContext, useState, useEffect } from 'react';

export type Language = 'zh' | 'en';

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const translations: Record<Language, Record<string, string>> = {
  en: {
    'nav.home': 'Home',
    'nav.activity': 'Activity',
    'nav.publish': 'Publish',
    'nav.cart': 'Cart',
    'nav.me': 'Me',
    'home.search': 'Search for restaurants, services...',
    'home.recommended': 'Recommended',
    'home.distance': 'Distance',
    'home.rating': 'Rating',
    'home.sales': 'Most Reviewed',
    'cat.food': 'Restaurants',
    'cat.delivery': 'Delivery',
    'cat.takeout': 'Takeout',
    'cat.services': 'Services',
    'cat.jobs': 'Jobs',
    'cat.housing': 'Housing',
    'cat.secondhand': 'Used',
    'cat.housekeeping': 'Cleaning',
    'cat.moving': 'Moving',
    'cat.repair': 'Repair',
    'cat.entertainment': 'Entertainment',
    'settings.title': 'Settings',
    'settings.account': 'ACCOUNT',
    'settings.profile': 'Profile & Account',
    'settings.notifications': 'Notifications',
    'settings.privacy': 'Privacy & Security',
    'settings.preferences': 'PREFERENCES',
    'settings.locations': 'Locations',
    'settings.language': 'Language',
    'settings.about': 'ABOUT',
    'settings.terms': 'Terms of Service',
    'settings.policy': 'Privacy Policy',
    'settings.version': 'Version',
    'settings.logout': 'Log Out',
    'profile.title': 'Me',
    'profile.balance': 'Balance',
    'profile.coupons': 'Coupons',
    'profile.points': 'Points',
    'profile.recent_spending': 'Recent Spending Trend',
    'profile.spending_subtitle': 'Shows your daily consumption fluctuations',
    'profile.total_spending': 'Total',
    'profile.highest_daily': 'Highest Daily Spending',
    'profile.health_score': 'Consumption Health',
    'profile.my_benefits': 'My Benefits',
    'profile.benefit_1_title': 'Special Discount',
    'profile.benefit_1_desc': '12% off at Yao Mountain',
    'profile.benefit_2_title': 'Free Delivery',
    'profile.benefit_2_desc': '2 vouchers left',
    'profile.notifications': 'Live Notifications',
    'profile.notifications_clear': 'Clear All',
    'profile.notifications_empty': 'No new notifications.',
    'profile.member_premium': 'Premium',
    'profile.member_vip': 'VIP',
    'profile.member_free': 'Upgrade',
    'profile.verified': 'Verified LianShan Account',
    'profile.coupon_unit': 'pcs',
    'profile.username': 'Mr. Xu',
    'merchant.open': 'Open',
    'merchant.reviews': 'reviews',
    'merchant.message': 'Message',
    'cart.title': 'Cart',
    'cart.empty': 'Cart is empty',
    'cart.checkout': 'Checkout',
    'order.title': 'Activity',
  },
  zh: {
    'nav.home': '首页',
    'nav.activity': '订单',
    'nav.publish': '发布',
    'nav.cart': '购物车',
    'nav.me': '我的',
    'home.search': '搜工作、房产、服务...',
    'home.recommended': '推荐信息',
    'home.distance': '距离',
    'home.rating': '好评',
    'home.sales': '销量',
    'cat.food': '美食',
    'cat.delivery': '外送',
    'cat.takeout': '自提',
    'cat.services': '服务',
    'cat.jobs': '招聘',
    'cat.housing': '房产',
    'cat.secondhand': '二手',
    'cat.housekeeping': '家政',
    'cat.moving': '搬家',
    'cat.repair': '维修',
    'cat.entertainment': '娱乐',
    'settings.title': '设置',
    'settings.account': '账号',
    'settings.profile': '个人资料与账号',
    'settings.notifications': '通知',
    'settings.privacy': '隐私与安全',
    'settings.preferences': '偏好设置',
    'settings.locations': '地址管理',
    'settings.language': '语言',
    'settings.about': '关于',
    'settings.terms': '服务条款',
    'settings.policy': '隐私政策',
    'settings.version': '版本',
    'settings.logout': '退出登录',
    'profile.title': '个人中心',
    'profile.balance': '余额 (元)',
    'profile.coupons': '优惠券',
    'profile.points': '尊贵积分',
    'profile.recent_spending': '近七天消费金额趋势',
    'profile.spending_subtitle': '展示您在连山同城的日常消费波动',
    'profile.total_spending': '七日总消费',
    'profile.highest_daily': '单日最高消费',
    'profile.health_score': '消费合理度',
    'profile.my_benefits': '我的同城权益',
    'profile.benefit_1_title': '柴火鸡特惠特权',
    'profile.benefit_1_desc': '瑶山人家专享 8.8 折',
    'profile.benefit_2_title': '免配送费体验券',
    'profile.benefit_2_desc': '还剩 2 张可用',
    'profile.notifications': '实时消息通知',
    'profile.notifications_clear': '清空消息',
    'profile.notifications_empty': '暂无新消息，去逛逛同城服务吧。',
    'profile.member_premium': '高级会员',
    'profile.member_vip': '普通会员',
    'profile.member_free': '升级会员',
    'profile.verified': '已绑定连山壮瑶同城实名认证账户',
    'profile.coupon_unit': '张',
    'profile.username': '徐先生',
    'merchant.open': '营业中',
    'merchant.reviews': '条评价',
    'merchant.message': '联系商家',
    'cart.title': '购物车',
    'cart.empty': '购物车空空如也',
    'cart.checkout': '去结算',
    'order.title': '订单记录',
  }
};

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<Language>('zh');

  useEffect(() => {
    const saved = localStorage.getItem('app_language') as Language;
    if (saved && (saved === 'zh' || saved === 'en')) {
      setLanguageState(saved);
    }
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem('app_language', lang);
  };

  const t = (key: string): string => {
    return translations[language][key] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
