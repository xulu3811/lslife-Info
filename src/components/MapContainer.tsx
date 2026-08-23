/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useRef, useState } from 'react';
import { MapPin, Navigation, Bike, Compass, ZoomIn, ZoomOut } from 'lucide-react';
import { Merchant } from '../types';

interface MapContainerProps {
  merchants: Merchant[];
  userLocation: { lat: number; lng: number; name: string };
  selectedMerchantId?: string | null;
  activeRiderLocation?: { lat: number; lng: number; name: string } | null;
  riderStatus?: string;
  isDarkMode?: boolean;
  onSelectMerchant?: (id: string) => void;
}

export default function MapContainer({
  merchants,
  userLocation,
  selectedMerchantId,
  activeRiderLocation,
  riderStatus,
  isDarkMode = false,
  onSelectMerchant,
}: MapContainerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [zoom, setZoom] = useState<number>(1);
  const [pan, setPan] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number }>({ x: 0, y: 0 });

  // Map coordinates projection to SVG viewBox (1000 x 1000 grid)
  // Lianshan center: Lat 24.4720, Lng 112.0810
  const centerLat = 24.4720;
  const centerLng = 112.0810;
  
  // Scale factor: roughly mapping 0.01 degrees of lat/lng to 300 SVG pixels
  const latToY = (lat: number) => {
    const diff = lat - centerLat;
    return 500 - diff * 35000; // inverted Y axis for maps
  };

  const lngToX = (lng: number) => {
    const diff = lng - centerLng;
    return 500 + diff * 35000;
  };

  const userX = lngToX(userLocation.lng);
  const userY = latToY(userLocation.lat);

  // Dragging handlers
  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    setDragStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return;
    setPan({
      x: e.clientX - dragStart.x,
      y: e.clientY - dragStart.y,
    });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Touch handlers for mobile adaptability
  const handleTouchStart = (e: React.TouchEvent) => {
    if (e.touches.length === 1) {
      setIsDragging(true);
      setDragStart({ x: e.touches[0].clientX - pan.x, y: e.touches[0].clientY - pan.y });
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isDragging || e.touches.length !== 1) return;
    setPan({
      x: e.touches[0].clientX - dragStart.x,
      y: e.touches[0].clientY - dragStart.y,
    });
  };

  // Center on user or rider
  const resetMap = () => {
    setZoom(1.1);
    setPan({ x: 0, y: 0 });
  };

  useEffect(() => {
    if (activeRiderLocation) {
      // Auto pan slightly to center rider & user
      const riderX = lngToX(activeRiderLocation.lng);
      const riderY = latToY(activeRiderLocation.lat);
      const midX = (userX + riderX) / 2;
      const midY = (userY + riderY) / 2;
      setPan({
        x: 500 - midX,
        y: 500 - midY,
      });
      setZoom(1.2);
    }
  }, [activeRiderLocation]);

  // SVG color palettes for light and dark
  const colors = {
    bg: isDarkMode ? '#111827' : '#f8fafc',
    grid: isDarkMode ? 'rgba(255,255,255,0.03)' : 'rgba(0,0,0,0.02)',
    river: isDarkMode ? '#1e3a8a' : '#bfdbfe',
    riverOutline: isDarkMode ? '#2563eb' : '#93c5fd',
    road: isDarkMode ? '#1f2937' : '#e2e8f0',
    roadLine: isDarkMode ? '#374151' : '#cbd5e1',
    userRing: isDarkMode ? 'rgba(59, 130, 246, 0.2)' : 'rgba(59, 130, 246, 0.15)',
    userDot: '#3b82f6',
    route: '#22c55e',
    park: isDarkMode ? '#14532d' : '#dcfce7',
    parkOutline: isDarkMode ? '#166534' : '#bbf7d0',
  };

  const selectedMerchant = merchants.find(m => m.id === selectedMerchantId);

  return (
    <div 
      className="relative w-full h-full overflow-hidden rounded-2xl border select-none transition-colors duration-300"
      style={{
        backgroundColor: colors.bg,
        borderColor: isDarkMode ? '#374151' : '#e2e8f0',
      }}
      ref={containerRef}
    >
      {/* Interactive Map canvas */}
      <div
        className="w-full h-full cursor-grab active:cursor-grabbing transition-transform duration-75"
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleMouseUp}
        style={{
          transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
          transformOrigin: '50% 50%',
        }}
      >
        <svg 
          viewBox="0 0 1000 1000" 
          className="w-[1000px] h-[1000px]"
          xmlns="http://www.w3.org/2000/svg"
        >
          {/* Map Grid */}
          <defs>
            <pattern id="grid" width="50" height="50" patternUnits="userSpaceOnUse">
              <rect width="50" height="50" fill="none" stroke={colors.grid} strokeWidth="1" />
            </pattern>
          </defs>
          <rect width="1000" height="1000" fill="url(#grid)" />

          {/* Park Area 1: Lianshan Ecological Park */}
          <path 
            d="M 100,200 Q 180,150 250,220 T 400,300 L 300,500 L 100,400 Z" 
            fill={colors.park} 
            stroke={colors.parkOutline} 
            strokeWidth="2"
            opacity="0.75"
          />
          <text x="200" y="300" fill={isDarkMode ? '#4ade80' : '#15803d'} fontSize="14" className="font-sans font-semibold opacity-60">
            连山九龙山生态园
          </text>

          {/* Park Area 2: Riverside Square */}
          <path 
            d="M 650,450 Q 750,400 850,480 T 900,650 L 750,700 Z" 
            fill={colors.park} 
            stroke={colors.parkOutline} 
            strokeWidth="2"
            opacity="0.6"
          />
          <text x="760" y="550" fill={isDarkMode ? '#4ade80' : '#15803d'} fontSize="14" className="font-sans font-semibold opacity-60">
            吉田滨河湿地公园
          </text>

          {/* River: Yoshida River */}
          <path
            d="M -100,600 Q 200,550 450,450 T 800,250 T 1100,100"
            fill="none"
            stroke={colors.river}
            strokeWidth="45"
            strokeLinecap="round"
          />
          <path
            d="M -100,600 Q 200,550 450,450 T 800,250 T 1100,100"
            fill="none"
            stroke={colors.riverOutline}
            strokeWidth="4"
            strokeDasharray="8 8"
            opacity="0.8"
          />
          <text x="450" y="420" fill={isDarkMode ? '#60a5fa' : '#2563eb'} fontSize="14" className="font-sans font-bold italic opacity-60 tracking-wider">
            吉田河 (Yoshida River)
          </text>

          {/* Main Roads */}
          {/* Ji'an North Road */}
          <path
            d="M 500,0 L 500,1000"
            fill="none"
            stroke={colors.road}
            strokeWidth="24"
          />
          <path
            d="M 500,0 L 500,1000"
            fill="none"
            stroke={colors.roadLine}
            strokeWidth="2"
            strokeDasharray="10 8"
          />

          {/* Chaoyang Road */}
          <path
            d="M 0,500 L 1000,500"
            fill="none"
            stroke={colors.road}
            strokeWidth="20"
          />
          <path
            d="M 0,500 L 1000,500"
            fill="none"
            stroke={colors.roadLine}
            strokeWidth="2"
            strokeDasharray="10 8"
          />

          {/* Chengbei Road (Diagonal) */}
          <path
            d="M 100,100 L 900,900"
            fill="none"
            stroke={colors.road}
            strokeWidth="16"
          />

          {/* Luming Middle Road */}
          <path
            d="M 200,800 Q 500,750 800,800"
            fill="none"
            stroke={colors.road}
            strokeWidth="16"
          />

          {/* Road Name Labels */}
          <g opacity="0.5" fontSize="11" fontWeight="bold" fill={isDarkMode ? '#9ca3af' : '#4b5563'} fontFamily="sans-serif">
            <text x="515" y="150" transform="rotate(90, 515, 150)">吉安北路</text>
            <text x="515" y="850" transform="rotate(90, 515, 850)">吉安南路</text>
            <text x="120" y="490">朝阳西路</text>
            <text x="820" y="490">朝阳东路</text>
            <text x="250" y="230" transform="rotate(45, 250, 230)">老街城北路</text>
            <text x="450" y="785">鹿鸣中路</text>
          </g>

          {/* Active Order Route Path (Dotted green line) */}
          {selectedMerchant && (
            <g>
              <path
                d={`M ${lngToX(selectedMerchant.longitude)},${latToY(selectedMerchant.latitude)} L ${userX},${userY}`}
                fill="none"
                stroke={colors.route}
                strokeWidth="4"
                strokeDasharray="6 6"
                className="animate-[dash_2s_linear_infinite]"
              />
              {/* Glowing signal rings at endpoints */}
              <circle cx={lngToX(selectedMerchant.longitude)} cy={latToY(selectedMerchant.latitude)} r="15" fill="none" stroke={colors.route} strokeWidth="1.5" opacity="0.6" className="animate-ping" />
            </g>
          )}

          {/* USER Location Pin */}
          <g>
            <circle cx={userX} cy={userY} r="35" fill={colors.userRing} className="animate-pulse" />
            <circle cx={userX} cy={userY} r="18" fill="rgba(59, 130, 246, 0.3)" />
            <circle cx={userX} cy={userY} r="8" fill={colors.userDot} stroke="#ffffff" strokeWidth="2" />
            {/* Address Banner */}
            <g transform={`translate(${userX - 85}, ${userY - 45})`}>
              <rect width="170" height="26" rx="6" fill={isDarkMode ? '#1f2937' : '#ffffff'} stroke="#3b82f6" strokeWidth="1.5" filter="drop-shadow(0px 2px 4px rgba(0,0,0,0.1))" />
              <text x="85" y="17" fill={isDarkMode ? '#ffffff' : '#1e293b'} fontSize="10" fontWeight="bold" textAnchor="middle">
                我的位置 (瑶香苑)
              </text>
            </g>
          </g>

          {/* MERCHANT Pins */}
          {merchants.map((merchant) => {
            const mX = lngToX(merchant.longitude);
            const mY = latToY(merchant.latitude);
            const isSelected = merchant.id === selectedMerchantId;

            return (
              <g 
                key={merchant.id}
                className="cursor-pointer group"
                onClick={() => onSelectMerchant && onSelectMerchant(merchant.id)}
              >
                {/* Ping animation if selected */}
                {isSelected && (
                  <circle cx={mX} cy={mY} r="30" fill="none" stroke="#f59e0b" strokeWidth="2" className="animate-ping" />
                )}

                {/* Pin shape */}
                <path
                  d={`M ${mX} ${mY} C ${mX - 14} ${mY - 14} ${mX - 18} ${mY - 32} ${mX} ${mY - 44} C ${mX + 18} ${mY - 32} ${mX + 14} ${mY - 14} ${mX} ${mY}`}
                  fill={isSelected ? '#f59e0b' : isDarkMode ? '#374151' : '#ef4444'}
                  stroke="#ffffff"
                  strokeWidth="2"
                  filter="drop-shadow(0px 3px 5px rgba(0,0,0,0.25))"
                />

                {/* Merchant logo or category shorthand inside pin */}
                <circle cx={mX} cy={mY - 26} r="10" fill="#ffffff" />
                <foreignObject x={mX - 8} y={mY - 34} width="16" height="16">
                  <div className="w-full h-full flex items-center justify-center text-[10px] font-bold text-gray-800">
                    {merchant.isFood ? '🍚' : '🛒'}
                  </div>
                </foreignObject>

                {/* Name Label */}
                <g transform={`translate(${mX - 70}, ${mY + 12})`}>
                  <rect 
                    width="140" 
                    height="20" 
                    rx="4" 
                    fill={isSelected ? '#f59e0b' : isDarkMode ? '#1f2937' : '#ffffff'} 
                    stroke={isSelected ? '#d97706' : isDarkMode ? '#4b5563' : '#e2e8f0'} 
                    strokeWidth="1" 
                    filter="drop-shadow(0px 2px 3px rgba(0,0,0,0.08))"
                  />
                  <text 
                    x="70" 
                    y="13" 
                    fill={isSelected ? '#ffffff' : isDarkMode ? '#e5e7eb' : '#1e293b'} 
                    fontSize="9" 
                    fontWeight="bold" 
                    textAnchor="middle"
                    className="truncate max-w-[130px]"
                  >
                    {merchant.name.split(' ')[0]}
                  </text>
                </g>
              </g>
            );
          })}

          {/* ACTIVE RIDER Position Pin (moving on scooter!) */}
          {activeRiderLocation && (
            <g>
              <circle cx={lngToX(activeRiderLocation.lng)} cy={latToY(activeRiderLocation.lat)} r="24" fill="rgba(34, 197, 94, 0.25)" className="animate-ping" />
              <circle cx={lngToX(activeRiderLocation.lng)} cy={latToY(activeRiderLocation.lat)} r="14" fill="#22c55e" stroke="#ffffff" strokeWidth="2.5" filter="drop-shadow(0px 4px 6px rgba(0,0,0,0.2))" />
              
              {/* Scooter Vector Icon inside pin */}
              <foreignObject x={lngToX(activeRiderLocation.lng) - 10} y={latToY(activeRiderLocation.lat) - 10} width="20" height="20">
                <div className="w-full h-full flex items-center justify-center text-white text-xs">
                  🛵
                </div>
              </foreignObject>

              {/* Rider Banner label */}
              <g transform={`translate(${lngToX(activeRiderLocation.lng) - 60}, ${latToY(activeRiderLocation.lat) - 38})`}>
                <rect width="120" height="20" rx="10" fill="#22c55e" stroke="#ffffff" strokeWidth="1" />
                <text x="60" y="13" fill="#ffffff" fontSize="9" fontWeight="bold" textAnchor="middle">
                  {riderStatus === 'delivering' ? '骑手配送中...' : '骑手赶往商家'}
                </text>
              </g>
            </g>
          )}
        </svg>
      </div>

      {/* Compass / Map Instrument Overlay */}
      <div className="absolute top-4 right-4 flex flex-col gap-2 z-10">
        <button 
          onClick={() => setZoom(prev => Math.min(prev + 0.15, 2.5))}
          className="p-2.5 rounded-full bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 shadow-md hover:bg-gray-50 dark:hover:bg-gray-700 transition"
          title="放大"
        >
          <ZoomIn size={18} />
        </button>
        <button 
          onClick={() => setZoom(prev => Math.max(prev - 0.15, 0.65))}
          className="p-2.5 rounded-full bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 shadow-md hover:bg-gray-50 dark:hover:bg-gray-700 transition"
          title="缩小"
        >
          <ZoomOut size={18} />
        </button>
        <button 
          onClick={resetMap}
          className="p-2.5 rounded-full bg-red-500 text-white shadow-md hover:bg-red-600 transition"
          title="居中重置"
        >
          <Compass size={18} className="animate-spin-slow" />
        </button>
      </div>

      {/* Map Legend Overlay */}
      <div className="absolute bottom-10 md:bottom-4 left-4 p-2.5 rounded-xl bg-white/95 dark:bg-gray-900/95 backdrop-blur-sm shadow-lg border border-gray-100 dark:border-gray-800 flex flex-col gap-1.5 z-10 text-[11px] max-w-[180px] transition-colors">
        <div className="font-bold text-gray-800 dark:text-gray-100">连山城区实时定位地图</div>
        <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
          <span className="w-2.5 h-2.5 rounded-full bg-red-500 ring-4 ring-red-500/25"></span>
          <span>我的地址: 瑶香苑小区</span>
        </div>
        <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
          <span className="w-2.5 h-2.5 rounded-full bg-red-500"></span>
          <span>周边特鲜美食商户</span>
        </div>
        <div className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
          <span className="w-2.5 h-2.5 rounded-full bg-gray-500 dark:bg-gray-400"></span>
          <span>生活百货/生鲜超市</span>
        </div>
        {activeRiderLocation && (
          <div className="flex items-center gap-2 text-green-600 dark:text-green-400 font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-green-500 animate-ping"></span>
            <span>美团专送骑手在线</span>
          </div>
        )}
      </div>
    </div>
  );
}
