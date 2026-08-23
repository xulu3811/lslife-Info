import re

with open('src/App.tsx', 'r') as f:
    content = f.read()

start_marker = '{/* Quick Services categories grid */}'
end_marker = '{/* Merchant list filters tab */}'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    old_section = content[start_idx:end_idx]
    
    new_section = """{/* Goods and Services Panel */}
            <div className="px-3 py-3 shrink-0">
              <div className="bg-white dark:bg-gray-900 rounded-[14px] shadow-sm border border-gray-100 dark:border-gray-800 p-3 pt-4">
                <div className="flex items-center justify-between mb-4 px-2">
                  <h2 className="text-[15px] font-extrabold tracking-tight text-gray-900 dark:text-white">商品和服务</h2>
                  <span className="text-[10px] text-red-600 dark:text-red-400 font-extrabold bg-red-50 dark:bg-red-500/10 px-2.5 py-[3px] rounded-full border border-red-100 dark:border-red-900/30">同城配送上门</span>
                </div>
                <div className="grid grid-cols-4 gap-y-5 gap-x-2 text-center pb-2">
                  
                  <button onClick={() => setSelectedCategory('food')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-green-50 dark:bg-green-500/10 flex items-center justify-center text-green-500 border border-green-100 dark:border-green-900/30">
                      <Carrot size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">蔬菜水果</span>
                  </button>

                  <button onClick={() => setSelectedCategory('flowers')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-pink-50 dark:bg-pink-500/10 flex items-center justify-center text-pink-500 border border-pink-100 dark:border-pink-900/30">
                      <Flower2 size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">鲜花</span>
                  </button>

                  <button onClick={() => setSelectedCategory('housekeeping')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-blue-50 dark:bg-blue-500/10 flex items-center justify-center text-blue-500 border border-blue-100 dark:border-blue-900/30">
                      <Sparkles size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">家政</span>
                  </button>

                  <button onClick={() => setSelectedCategory('plumbing')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-cyan-50 dark:bg-cyan-500/10 flex items-center justify-center text-cyan-500 border border-cyan-100 dark:border-cyan-900/30">
                      <Wrench size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">水电维修</span>
                  </button>

                  <button onClick={() => setSelectedCategory('appliance')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-indigo-50 dark:bg-indigo-500/10 flex items-center justify-center text-indigo-500 border border-indigo-100 dark:border-indigo-900/30">
                      <Plug size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">电器维修</span>
                  </button>

                  <button onClick={() => setSelectedCategory('education')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-purple-50 dark:bg-purple-500/10 flex items-center justify-center text-purple-500 border border-purple-100 dark:border-purple-900/30">
                      <GraduationCap size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">教育培训</span>
                  </button>

                  <button onClick={() => setSelectedCategory('secondhand')} className="flex flex-col items-center gap-1.5 p-1 transition cursor-pointer hover:opacity-80">
                    <div className="w-10 h-10 rounded-[12px] bg-amber-50 dark:bg-amber-500/10 flex items-center justify-center text-amber-500 border border-amber-100 dark:border-amber-900/30">
                      <Package size={20} strokeWidth={2} />
                    </div>
                    <span className="text-[11px] font-bold text-gray-700 dark:text-gray-300">个人闲置</span>
                  </button>

                </div>
              </div>
            </div>
            
            """
            
    content = content[:start_idx] + new_section + content[end_idx:]
    
    with open('src/App.tsx', 'w') as f:
        f.write(content)
else:
    print("Markers not found")

