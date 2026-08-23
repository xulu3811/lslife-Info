/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { Star, MessageCircle, CheckCircle } from 'lucide-react';
import { Merchant } from '../types';

interface MerchantCardProps {
  key?: React.Key | string;
  merchant: Merchant;
  isDarkMode?: boolean;
  onClick: () => void;
}

export default function MerchantCard({
  merchant,
  isDarkMode = false,
  onClick,
}: MerchantCardProps) {
  
  // Render stars based on rating
  const renderStars = (rating: number) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <Star 
          key={i} 
          size={11} 
          className={i <= Math.round(rating) ? 'text-[#FF1A1A] fill-[#FF1A1A]' : 'text-gray-300 fill-gray-300 dark:text-gray-600 dark:fill-gray-600'} 
        />
      );
    }
    return stars;
  };

  return (
    <div
      onClick={onClick}
      className={`group flex gap-3 py-3 px-3 border-b transition duration-300 cursor-pointer ${
        isDarkMode
          ? 'border-gray-800/80 hover:bg-gray-900/40'
          : 'border-gray-100 hover:bg-gray-50/50'
      }`}
    >
      {/* Merchant Image (Left side like Yelp) */}
      <div className="relative w-12 h-12 shrink-0 rounded-md overflow-hidden bg-gray-100 dark:bg-gray-800">
        <img
          src={merchant.logo}
          alt={merchant.name}
          referrerPolicy="no-referrer"
          className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
        />
      </div>

      {/* Merchant Content */}
      <div className="flex-1 min-w-0 flex flex-col">
        <div>
          <h3 className="font-extrabold text-[14px] text-gray-900 dark:text-white truncate group-hover:text-[#FF1A1A] transition duration-200">
            {merchant.name}
          </h3>
          
          {/* Ratings */}
          <div className="flex items-center gap-1.5 mt-1">
            <div className="flex bg-red-50 dark:bg-red-500/10 px-1 py-0.5 rounded">
              {renderStars(merchant.rating)}
            </div>
            <span className="text-[11px] font-medium text-gray-600 dark:text-gray-400">
              {Math.floor(merchant.sales * 1.5)} reviews
            </span>
          </div>

          {/* Tags & Price */}
          <div className="flex flex-wrap items-center gap-1.5 mt-1.5 text-[11px] text-gray-500 dark:text-gray-400">
            {merchant.tags.slice(0, 2).map((tag, idx) => (
              <span key={idx} className="hover:underline cursor-pointer">
                {tag}
              </span>
            ))}
            <span>•</span>
            <span className="text-gray-500">
              {merchant.avgPrice > 50 ? '$$$' : merchant.avgPrice > 20 ? '$$' : '$'}
            </span>
            <span>•</span>
            <span className="truncate">{merchant.distance} km</span>
          </div>
        </div>
        
        {/* Review Snippet (Yelp Style) */}
        <div className="mt-2.5 flex items-start gap-2 text-[10px] text-gray-600 dark:text-gray-300">
          <MessageCircle size={11} className="text-gray-400 shrink-0 mt-0.5" />
          <p className="line-clamp-2">
            "The food here is amazing and the service is great. Highly recommended! Will definitely come back again."
          </p>
        </div>

        {/* Highlights */}
        <div className="mt-2 flex items-center gap-2">
          <span className="inline-flex items-center gap-1 bg-green-50 text-green-700 dark:bg-green-500/10 dark:text-green-400 px-1.5 py-[1px] rounded text-[9px] font-bold">
            <CheckCircle size={9} /> 营业中
          </span>
          {merchant.deliveryFee > 0 && (
             <span className="inline-flex items-center gap-1 bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300 px-1.5 py-[1px] rounded text-[9px] font-bold">
               提供外卖
             </span>
          )}
        </div>
      </div>
    </div>
  );
}
