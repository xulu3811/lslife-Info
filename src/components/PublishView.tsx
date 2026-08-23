import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Briefcase, 
  Clock, 
  Home, 
  Recycle, 
  Heart, 
  Utensils, 
  Sparkles, 
  Truck, 
  Zap, 
  Tv,
  X,
  Camera,
  MapPin,
  Send,
  CheckCircle2,
  AlertCircle,
  Eye,
  ChevronLeft,
  Crown,
  Carrot
} from 'lucide-react';

interface PublishViewProps {
  isDarkMode?: boolean;
  membershipTier: 'free' | 'vip' | 'premium';
  publishedCount: number;
  onShowSubscription: () => void;
  onPublishSuccess: () => void;
}

export default function PublishView({ 
  isDarkMode = false,
  membershipTier,
  publishedCount,
  onShowSubscription,
  onPublishSuccess
}: PublishViewProps) {
  const [step, setStep] = useState<1 | 2>(1);
  const [agreed, setAgreed] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<{id: string, name: string, icon: any, color: string} | null>(null);
  const [images, setImages] = useState<string[]>([]);
  const [isPreviewMode, setIsPreviewMode] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: ''
  });
  
  const categories = [
    { id: 'job', name: '招聘', icon: Briefcase, color: 'from-red-400 to-red-500' },
    { id: 'house', name: '房屋租售', icon: Home, color: 'from-emerald-400 to-emerald-500' },
    { id: 'housekeeping', name: '家政保洁', icon: Sparkles, color: 'from-purple-400 to-purple-500' },
    { id: 'maintenance', name: '水电维修', icon: Zap, color: 'from-cyan-400 to-cyan-500' },
    { id: 'moving', name: '货运搬家', icon: Truck, color: 'from-amber-400 to-amber-500' },
    { id: 'veggies', name: '水果蔬菜', icon: Carrot, color: 'from-green-400 to-green-500' },
    { id: 'secondhand', name: '个人闲置', icon: Recycle, color: 'from-orange-400 to-orange-500' },
  ];

  const limits = { free: 10, vip: 20, premium: 50 };
  
  const handleCategorySelect = (cat: any) => {
    if (publishedCount >= limits[membershipTier]) {
      onShowSubscription();
      return;
    }
    setSelectedCategory(cat);
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newImages: string[] = [];
      const filesArray = Array.from(e.target.files);
      filesArray.forEach((file: File) => {
        const reader = new FileReader();
        reader.onload = (event) => {
          if (event.target?.result) {
            newImages.push(event.target.result as string);
            if (newImages.length === filesArray.length) {
              setImages(prev => [...prev, ...newImages].slice(0, 9));
            }
          }
        };
        reader.readAsDataURL(file);
      });
    }
  };

  const handlePublish = (e: React.FormEvent | React.MouseEvent) => {
    e.preventDefault();
    onPublishSuccess();
    alert('信息发布成功！');
    setSelectedCategory(null);
    setStep(1);
    setAgreed(false);
    setImages([]);
    setIsPreviewMode(false);
    setFormData({ title: '', description: '', price: '' });
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-gray-50/50 dark:bg-black overflow-y-auto pb-32">
      <AnimatePresence mode="wait">
        {step === 1 ? (
          <motion.div 
            key="step1"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            className="flex-1 flex flex-col p-4 max-w-lg mx-auto w-full"
          >
            <div className="flex flex-col items-center justify-center pt-12 pb-8">
              <div className="w-14 h-14 rounded-3xl bg-red-50 dark:bg-red-500/10 flex items-center justify-center text-red-500 mb-6">
                <AlertCircle size={32} strokeWidth={2.5} />
              </div>
              <h2 className="text-[17px] font-black tracking-tight text-gray-900 dark:text-white mb-2">发布须知</h2>
              <p className="text-[13px] text-gray-500 dark:text-gray-400 text-center max-w-[260px]">
                为了维护良好的社区环境，发布信息前请仔细阅读并同意以下规范
              </p>
            </div>

            <div className="bg-white dark:bg-gray-900 rounded-[2rem] p-4 shadow-sm border border-gray-100 dark:border-gray-800 flex-1 flex flex-col">
              <ul className="space-y-4 text-[13px] text-gray-600 dark:text-gray-300 font-medium">
                <li className="flex gap-3">
                  <span className="shrink-0 w-5 h-5 rounded-full bg-red-50 dark:bg-red-500/20 text-red-500 flex items-center justify-center font-bold text-xs">1</span>
                  <span>请确保发布的内容真实、合法、有效，不包含虚假信息。</span>
                </li>
                <li className="flex gap-3">
                  <span className="shrink-0 w-5 h-5 rounded-full bg-red-50 dark:bg-red-500/20 text-red-500 flex items-center justify-center font-bold text-xs">2</span>
                  <span>严禁发布涉及黄赌毒、暴力、诈骗等违法违规内容。</span>
                </li>
                <li className="flex gap-3">
                  <span className="shrink-0 w-5 h-5 rounded-full bg-red-50 dark:bg-red-500/20 text-red-500 flex items-center justify-center font-bold text-xs">3</span>
                  <span>请勿频繁发布重复内容或垃圾广告，影响他人体验。</span>
                </li>
                <li className="flex gap-3">
                  <span className="shrink-0 w-5 h-5 rounded-full bg-red-50 dark:bg-red-500/20 text-red-500 flex items-center justify-center font-bold text-xs">4</span>
                  <span>平台有权对违规信息进行下架处理，并对违规账号进行封禁。</span>
                </li>
              </ul>
            </div>

            <div className="mt-8 flex flex-col gap-4">
              <label className="flex items-center gap-3 cursor-pointer group">
                <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors ${agreed ? 'bg-red-500 border-red-500' : 'border-gray-300 dark:border-gray-600 group-hover:border-red-400'}`}>
                  {agreed && <CheckCircle2 size={12} className="text-white" strokeWidth={4} />}
                </div>
                <span className="text-[13px] font-medium text-gray-600 dark:text-gray-400">
                  我已阅读并同意 <span className="text-red-500 font-bold">《同城信息发布协议》</span>
                </span>
                {/* visually hidden checkbox */}
                <input 
                  type="checkbox" 
                  className="hidden" 
                  checked={agreed}
                  onChange={(e) => setAgreed(e.target.checked)}
                />
              </label>

              <button 
                onClick={() => setStep(2)}
                disabled={!agreed}
                className={`w-full py-4 rounded-2xl font-black text-[15px] transition-all duration-300 shadow-sm ${agreed ? 'bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white shadow-[0_8px_20px_rgba(59,130,246,0.3)] active:scale-95' : 'bg-gray-100 dark:bg-gray-800 text-gray-400 cursor-not-allowed'}`}
              >
                下一步，选择类型
              </button>
            </div>
          </motion.div>
        ) : (
          <motion.div 
            key="step2"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 20 }}
            className="flex-1 flex flex-col"
          >
            <div className="px-4 pt-10 pb-4 flex items-center gap-4">
              <button 
                onClick={() => setStep(1)}
                className="w-8 h-8 rounded-full bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 flex items-center justify-center text-gray-600 dark:text-gray-400 shadow-sm hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors shrink-0"
              >
                <X size={16} strokeWidth={2.5} />
              </button>
              <div className="flex-1">
                <motion.h2 
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="text-[17px] font-black tracking-tight text-gray-900 dark:text-white"
                >
                  你想发布什么？
                </motion.h2>
                <motion.div 
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 }}
                  className="flex items-center justify-between mt-2"
                >
                  <span className="text-[13px] font-medium text-gray-500 dark:text-gray-400">
                    选择一个分类，让同城的人看到你的信息
                  </span>
                  <button 
                    onClick={onShowSubscription}
                    className="flex items-center gap-1 bg-gradient-to-r from-amber-50 to-orange-50 dark:from-amber-500/10 dark:to-orange-500/10 border border-amber-200 dark:border-amber-800 text-amber-600 dark:text-amber-500 px-2.5 py-1 rounded-full text-[11px] font-bold shadow-sm transition active:scale-95"
                  >
                    <Crown size={12} strokeWidth={3} />
                    <span>本月已发 {publishedCount}/{limits[membershipTier]}</span>
                  </button>
                </motion.div>
              </div>
            </div>

            <div className="px-4 grid grid-cols-2 gap-4 pb-10">
              {categories.map((cat, idx) => {
                const Icon = cat.icon;
                return (
                  <motion.button
                    key={cat.id}
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.1 + idx * 0.05 }}
                    onClick={() => handleCategorySelect(cat)}
                    className="relative overflow-hidden flex flex-col items-center justify-center p-4 rounded-3xl bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)] active:scale-95 transition-transform"
                  >
                    <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${cat.color} flex items-center justify-center text-white mb-3 shadow-sm`}>
                      <Icon size={24} strokeWidth={2.5} />
                    </div>
                    <span className="text-[14px] font-bold text-gray-900 dark:text-white">{cat.name}</span>
                  </motion.button>
                );
              })}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {selectedCategory && (
          <motion.div
            initial={{ opacity: 0, y: '100%' }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: '100%' }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            className="absolute inset-0 z-50 bg-white dark:bg-gray-950 flex flex-col"
          >
            {isPreviewMode ? (
              <div className="flex-1 flex flex-col h-full bg-gray-50 dark:bg-gray-950">
                <div className="px-5 py-4 border-b border-gray-100 dark:border-gray-800 flex items-center justify-between bg-white dark:bg-gray-900 sticky top-0 z-10">
                  <button 
                    onClick={() => setIsPreviewMode(false)}
                    className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 transition"
                  >
                    <ChevronLeft size={20} strokeWidth={2.5} />
                  </button>
                  <h3 className="font-extrabold text-gray-900 dark:text-white text-base">发布预览</h3>
                  <div className="w-8" />
                </div>

                <div className="flex-1 overflow-y-auto p-4">
                  <div className="bg-white dark:bg-gray-900 rounded-[2rem] overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.1)] border border-gray-100 dark:border-gray-800">
                    {images.length > 0 ? (
                      <div className="w-full aspect-video bg-gray-100 dark:bg-gray-800 relative">
                        <img src={images[0]} className="w-full h-full object-cover" alt="Preview cover" />
                        {images.length > 1 && (
                          <div className="absolute bottom-3 right-3 bg-black/60 backdrop-blur-md text-white text-[11px] font-bold px-2 py-1 rounded-lg flex items-center gap-1">
                            <Camera size={12} />
                            1 / {images.length}
                          </div>
                        )}
                      </div>
                    ) : (
                      <div className="w-full aspect-video bg-gray-50 dark:bg-gray-800 flex flex-col items-center justify-center text-gray-400">
                        <Camera size={32} className="opacity-50 mb-2" />
                        <span className="text-xs font-medium">暂无图片</span>
                      </div>
                    )}
                    
                    <div className="p-4">
                      <div className="flex items-center gap-2 mb-4">
                        <div className={`px-2.5 py-1 rounded-lg bg-gradient-to-br ${selectedCategory.color} text-white text-[10px] font-black`}>
                          {selectedCategory.name}
                        </div>
                        <span className="text-xs text-gray-400 font-medium">刚刚发布</span>
                      </div>
                      
                      <h2 className="text-[20px] font-black text-gray-900 dark:text-white leading-tight break-all mb-3">
                        {formData.title || '未填写标题'}
                      </h2>
                      
                      {formData.price && (
                        <div className="text-[17px] font-black text-red-500 mb-5 tracking-tight">
                          <span className="text-sm mr-0.5">¥</span>{formData.price}
                        </div>
                      )}
                      
                      <div className="w-8 h-1 bg-gray-100 dark:bg-gray-800 rounded-full mb-5" />
                      
                      <p className="text-[15px] text-gray-600 dark:text-gray-300 leading-relaxed whitespace-pre-wrap break-all mb-6">
                        {formData.description || '未填写详细描述'}
                      </p>
                      
                      <div className="flex items-center gap-2 text-[13px] font-medium text-gray-500 bg-gray-50 dark:bg-gray-800/50 p-3.5 rounded-2xl">
                        <MapPin size={16} className="text-red-500 shrink-0" />
                        <span className="truncate">当前位置</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="p-4 border-t border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-950 shrink-0 mb-4 flex gap-3">
                  <button 
                    onClick={() => setIsPreviewMode(false)}
                    className="flex-1 py-4 rounded-2xl bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-black text-[15px] transition active:scale-95"
                  >
                    返回修改
                  </button>
                  <button 
                    onClick={handlePublish}
                    className="flex-1 py-4 rounded-2xl bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white font-black text-[15px] shadow-[0_8px_20px_rgba(59,130,246,0.3)] transition active:scale-95 flex items-center justify-center gap-2"
                  >
                    <Send size={18} />
                    确认发布
                  </button>
                </div>
              </div>
            ) : (
              <>
                <div className="px-5 py-4 border-b border-gray-100 dark:border-gray-800 flex items-center justify-between bg-white dark:bg-gray-950 sticky top-0 z-10">
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${selectedCategory.color} flex items-center justify-center text-white`}>
                      <selectedCategory.icon size={20} strokeWidth={2.5} />
                    </div>
                    <div>
                      <h3 className="font-extrabold text-gray-900 dark:text-white text-base">发布{selectedCategory.name}</h3>
                      <p className="text-[11px] font-medium text-gray-500">填写详细信息</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => setSelectedCategory(null)}
                    className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 transition"
                  >
                    <X size={18} strokeWidth={2.5} />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto p-4">
                  <form onSubmit={handlePublish} className="flex flex-col gap-4 pb-20">
                    {/* Image Upload Area */}
                    <div className="flex gap-3 overflow-x-auto pb-2 -mx-2 px-2 snap-x">
                      {images.map((img, idx) => (
                        <div key={idx} className="relative shrink-0 w-24 h-24 rounded-2xl overflow-hidden border border-gray-200 dark:border-gray-800 snap-center shadow-sm">
                          <img src={img} alt={`上传预览 ${idx + 1}`} className="w-full h-full object-cover" />
                          <button 
                            type="button"
                            onClick={() => setImages(prev => prev.filter((_, i) => i !== idx))}
                            className="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/60 text-white flex items-center justify-center backdrop-blur-md hover:bg-black/80 transition-colors"
                          >
                            <X size={14} strokeWidth={3} />
                          </button>
                        </div>
                      ))}
                      
                      {images.length < 9 && (
                        <label className="shrink-0 w-24 h-24 rounded-2xl border-2 border-dashed border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 flex flex-col items-center justify-center text-gray-400 hover:text-red-500 hover:border-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all cursor-pointer snap-center">
                          <Camera size={24} className="mb-1" />
                          <span className="text-[11px] font-bold text-gray-500">添加图片</span>
                          <span className="text-[9px] mt-0.5 text-gray-400">{images.length}/9</span>
                          <input 
                            type="file" 
                            accept="image/*" 
                            className="hidden" 
                            onChange={handleImageUpload}
                            multiple
                          />
                        </label>
                      )}
                    </div>

                    <div className="flex flex-col gap-2">
                      <label className="text-[13px] font-bold text-gray-700 dark:text-gray-300 ml-1">标题</label>
                      <input 
                        type="text" 
                        placeholder="请输入吸引人的标题..." 
                        value={formData.title}
                        onChange={(e) => setFormData(prev => ({...prev, title: e.target.value}))}
                        className="w-full bg-gray-50 dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl px-4 py-3.5 text-[15px] text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-black transition-all"
                        required
                      />
                    </div>

                    <div className="flex flex-col gap-2">
                      <label className="text-[13px] font-bold text-gray-700 dark:text-gray-300 ml-1">详细描述</label>
                      <textarea 
                        placeholder="描述一下详细情况，如图文并茂更能吸引人哦..." 
                        rows={4}
                        value={formData.description}
                        onChange={(e) => setFormData(prev => ({...prev, description: e.target.value}))}
                        className="w-full bg-gray-50 dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl px-4 py-3.5 text-[15px] text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-black transition-all resize-none"
                        required
                      />
                    </div>
                    
                    <div className="flex flex-col gap-2">
                      <label className="text-[13px] font-bold text-gray-700 dark:text-gray-300 ml-1">价格/薪资</label>
                      <div className="relative">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 font-bold">¥</span>
                        <input 
                          type="number" 
                          placeholder="0.00" 
                          value={formData.price}
                          onChange={(e) => setFormData(prev => ({...prev, price: e.target.value}))}
                          className="w-full bg-gray-50 dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl pl-8 pr-4 py-3.5 text-[15px] text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-black transition-all"
                        />
                      </div>
                    </div>

                    <div className="flex flex-col gap-2">
                      <label className="text-[13px] font-bold text-gray-700 dark:text-gray-300 ml-1">位置信息</label>
                      <div className="flex items-center gap-3 bg-gray-50 dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl px-4 py-3.5 text-gray-500 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition">
                        <MapPin size={18} className="text-red-500" />
                        <span className="text-[15px] font-medium text-gray-900 dark:text-white flex-1">点击获取当前位置</span>
                        <span className="text-xs font-bold text-red-500">定位</span>
                      </div>
                    </div>
                  </form>
                </div>

                <div className="p-4 border-t border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-950 shrink-0 mb-4 flex gap-3">
                  <button 
                    type="button"
                    onClick={() => setIsPreviewMode(true)}
                    className="flex-1 py-4 rounded-2xl bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20 text-red-600 dark:text-red-400 font-black text-[16px] transition active:scale-95 flex items-center justify-center gap-2"
                  >
                    <Eye size={18} />
                    预览
                  </button>
                  <button 
                    onClick={handlePublish}
                    className="flex-[2] py-4 rounded-2xl bg-gradient-to-r from-red-600 to-indigo-600 hover:opacity-90 text-white font-black text-[16px] shadow-[0_8px_20px_rgba(59,130,246,0.3)] transition active:scale-95 flex items-center justify-center gap-2"
                  >
                    <Send size={18} />
                    立即发布
                  </button>
                </div>
              </>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
