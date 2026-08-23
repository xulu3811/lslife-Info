/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface MenuItem {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  desc: string;
  sales: number;
  image: string;
  category: string;
  rating?: number;
}

export interface Merchant {
  id: string;
  name: string;
  rating: number;
  distance: number; // in km
  sales: number; // monthly sales
  avgPrice: number; // per person
  tags: string[];
  deliveryFee: number;
  deliveryTime: number; // in mins
  logo: string;
  banner: string;
  isFood: boolean;
  category?: string;
  latitude: number;
  longitude: number;
  items: MenuItem[];
  description: string;
  address: string;
  phone: string;
}

export interface CartItem {
  item: MenuItem;
  quantity: number;
}

export interface Order {
  id: string;
  merchantId: string;
  merchantName: string;
  merchantLogo: string;
  items: CartItem[];
  totalAmount: number;
  deliveryFee: number;
  status: 'pending' | 'preparing' | 'delivering' | 'delivered';
  paymentMethod: 'alipay' | 'wechat' | 'wallet';
  deliveryAddress: {
    name: string;
    phone: string;
    address: string;
  };
  rider?: {
    name: string;
    phone: string;
    lat: number;
    lng: number;
    avatar: string;
  };
  createdAt: string;
}

export interface LocationCoordinates {
  lat: number;
  lng: number;
  name: string;
}
