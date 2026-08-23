import re

with open('src/App.tsx', 'r') as f:
    content = f.read()

# 1. Add recommendedMerchants useMemo
usememo_marker = "const filteredMerchants = useMemo(() => {"
rec_logic = """
  const recommendedMerchants = useMemo(() => {
    // 模拟基于用户历史购买或点击记录的推荐算法
    return [...merchants].sort((a, b) => b.rating - a.rating).slice(0, 3);
  }, [merchants]);

  const """
content = content.replace(usememo_marker, rec_logic + "filteredMerchants = useMemo(() => {")

# 2. Add the UI right before filters tab
filter_marker = "{/* Merchant list filters tab */}"
rec_ui = """{/* Guess You Like (Smart Recommendations) */}
            {recommendedMerchants.length > 0 && (
              <div className="px-3 pt-2 pb-1 shrink-0">
                <div className="flex items-center justify-between mb-3 px-1">
                  <h2 className="text-[15px] font-extrabold tracking-tight text-gray-900 dark:text-white flex items-center gap-1.5">
                    <Heart size={16} className="text-[#FF1A1A] fill-[#FF1A1A]" />
                    猜你喜欢
                  </h2>
                  <span className="text-[10px] text-gray-500 font-medium">基于近期点击记录推荐</span>
                </div>
                <div className="flex gap-3 overflow-x-auto no-scrollbar px-1 pb-2">
                  {recommendedMerchants.map(merchant => (
                    <div 
                      key={merchant.id}
                      onClick={() => {
                        setHighlightedItemId(null);
                        setSelectedMerchant(merchant);
                        navigateToScreen('detail');
                      }}
                      className="w-[140px] shrink-0 bg-white dark:bg-gray-900 rounded-[14px] shadow-sm border border-gray-100 dark:border-gray-800 overflow-hidden cursor-pointer hover:shadow-md transition"
                    >
                      <div className="h-[90px] relative">
                        <img src={merchant.logo} alt={merchant.name} className="w-full h-full object-cover" />
                        <div className="absolute top-1.5 right-1.5 bg-black/50 backdrop-blur-sm px-1.5 py-0.5 rounded text-[9px] text-white font-bold flex items-center gap-0.5">
                          <Star size={8} className="fill-amber-400 text-amber-400" />
                          {merchant.rating}
                        </div>
                      </div>
                      <div className="p-2.5">
                        <h3 className="text-[12px] font-bold text-gray-900 dark:text-white truncate mb-1">{merchant.name}</h3>
                        <div className="flex items-center gap-1 text-[10px] text-gray-500 mb-1.5">
                          <MapPin size={10} />
                          <span className="truncate">{merchant.distance} km</span>
                        </div>
                        <div className="flex flex-wrap gap-1 h-[16px] overflow-hidden">
                          {merchant.tags.slice(0, 2).map((tag, idx) => (
                            <span key={idx} className="bg-red-50 dark:bg-red-500/10 text-red-500 px-1 py-[1px] rounded text-[8px]">
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

            """
content = content.replace(filter_marker, rec_ui + filter_marker)

with open('src/App.tsx', 'w') as f:
    f.write(content)
