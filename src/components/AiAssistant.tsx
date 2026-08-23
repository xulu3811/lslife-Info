/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { MessageSquare, Sparkles, Send, X, Bot, ArrowRight, CornerDownLeft, ShoppingCart } from 'lucide-react';

interface AiAssistantProps {
  isDarkMode?: boolean;
  onSelectRecommendedItem: (merchantId: string, itemId: string) => void;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  recommendations?: Array<{
    merchantId: string;
    itemId: string;
    name: string;
    price: number;
  }>;
}

export default function AiAssistant({
  isDarkMode = false,
  onSelectRecommendedItem,
}: AiAssistantProps) {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [inputValue, setInputValue] = useState<string>('');
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'bot',
      text: '您好！我是“连山同城”的智能AI生活助手。😊\n\n我可以帮您推荐连山当地特色美食（如瑶家柴火鸡、传统大汤糍）、寻找最新鲜的超市水果或下午茶饮品。请问您今天想来点什么美味？',
    },
  ]);
  const [isTyping, setIsTyping] = useState<boolean>(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isTyping]);

  const quickQuestions = [
    '推荐一下本地特色大汤糍 🍡',
    '我想吃瑶家柴火鸡，有优惠吗？ 🍗',
    '下午茶犯困想喝咖啡配大福 ☕',
    '生鲜超市有什么多汁的水果？ 🍊',
  ];

  const handleSend = async (text: string) => {
    if (!text.trim()) return;

    const userMsgId = 'user_' + Date.now();
    const newMessages: ChatMessage[] = [
      ...messages,
      { id: userMsgId, sender: 'user', text },
    ];
    setMessages(newMessages);
    setInputValue('');
    setIsTyping(true);

    try {
      const response = await fetch('/api/gemini/recommend', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: text }),
      });

      if (!response.ok) {
        throw new Error('AI 服务请求异常');
      }

      const data = await response.json();
      
      setMessages(prev => [
        ...prev,
        {
          id: 'bot_' + Date.now(),
          sender: 'bot',
          text: data.reply || '连山同城AI暂时无法解析该美食推荐。建议直接浏览下方精选商家。',
          recommendations: data.recommendations || [],
        },
      ]);
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          id: 'bot_err_' + Date.now(),
          sender: 'bot',
          text: '抱歉，连山高山地区信号不好，小助手被风吹跑了... 🍃\n您可以试试：\n\n- 招牌瑶家柴火鸡 (半只装+配菜) (瑶山人家柴火鸡)\n- 经典咸大汤糍 (连山大汤糍)',
          recommendations: [
            { merchantId: 'm1', itemId: 'm1_i1', name: '招牌瑶家柴火鸡', price: 68 },
            { merchantId: 'm2', itemId: 'm2_i1', name: '经典咸大汤糍', price: 10 }
          ],
        },
      ]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <>
      {/* Floating Assist Bubble */}
      <div className="fixed bottom-6 right-6 z-40">
        <button
          onClick={() => setIsOpen(true)}
          className="flex items-center gap-2 p-3.5 rounded-full bg-gradient-to-r from-red-500 to-indigo-600 hover:from-red-600 hover:to-indigo-700 text-white shadow-xl hover:scale-105 transition duration-200 cursor-pointer border border-white/20"
        >
          <div className="relative">
            <Bot size={22} className="animate-pulse" />
            <span className="absolute -top-1 -right-1 flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-pink-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-pink-500"></span>
            </span>
          </div>
          <span className="text-xs font-extrabold tracking-wider pr-1">连山AI管家</span>
        </button>
      </div>

      {/* Slide-out Sidebar Panel */}
      <AnimatePresence>
        {isOpen && (
          <div className="fixed inset-0 z-50 flex justify-end">
            {/* Backdrop click to close */}
            <div 
              className="absolute inset-0 bg-black/40 backdrop-blur-xs"
              onClick={() => setIsOpen(false)}
            />

            {/* Sidebar Body */}
            <motion.div
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 220 }}
              className={`relative w-full max-w-md h-full flex flex-col shadow-2xl border-l transition-colors duration-300 ${
                isDarkMode ? 'bg-gray-950 border-gray-800 text-white' : 'bg-white border-gray-100 text-gray-950'
              }`}
            >
              {/* Header */}
              <div className="flex items-center justify-between p-4 border-b border-gray-100 dark:border-gray-800 shrink-0 bg-gradient-to-r from-red-500/5 to-indigo-500/5">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-red-500 to-indigo-600 flex items-center justify-center text-white">
                    <Sparkles size={18} className="animate-pulse" />
                  </div>
                  <div>
                    <h3 className="font-extrabold text-sm tracking-wide">连山AI生活管家</h3>
                    <p className="text-[10px] text-green-500 font-semibold flex items-center gap-1 mt-0.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-ping"></span>
                      <span>Gemini 3.5 智能推荐大脑在线</span>
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setIsOpen(false)}
                  className="p-1.5 rounded-full text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition cursor-pointer"
                >
                  <X size={18} />
                </button>
              </div>

              {/* Chat Messages viewport */}
              <div 
                className="flex-1 overflow-y-auto p-4 space-y-4"
                ref={scrollRef}
              >
                {messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`flex items-start gap-2.5 ${msg.sender === 'user' ? 'justify-end' : ''}`}
                  >
                    {msg.sender === 'bot' && (
                      <div className="w-8 h-8 rounded-lg bg-red-500/10 flex items-center justify-center text-red-500 shrink-0">
                        <Bot size={16} />
                      </div>
                    )}
                    <div className="max-w-[80%] flex flex-col gap-1.5">
                      <div
                        className={`p-3.5 rounded-2xl text-xs leading-relaxed whitespace-pre-wrap font-medium shadow-sm ${
                          msg.sender === 'user'
                            ? 'bg-gradient-to-br from-red-500 to-indigo-600 text-white rounded-tr-none'
                            : isDarkMode ? 'bg-gray-900 text-gray-100 rounded-tl-none border border-gray-800' : 'bg-gray-100 text-gray-800 rounded-tl-none'
                        }`}
                      >
                        {msg.text}
                      </div>

                      {/* Render accompanying dish items if returned by AI */}
                      {msg.recommendations && msg.recommendations.length > 0 && (
                        <div className="space-y-2 mt-1">
                          <p className="text-[10px] font-extrabold text-gray-400 tracking-wider">
                            为您匹配的同城美食：
                          </p>
                          {msg.recommendations.map((rec, idx) => (
                            <div
                              key={idx}
                              className={`flex items-center justify-between p-3 rounded-xl border transition ${
                                isDarkMode ? 'bg-gray-900/60 border-gray-800 hover:border-red-500' : 'bg-white border-gray-100 hover:border-red-500 shadow-sm'
                              }`}
                            >
                              <div>
                                <h4 className="text-xs font-bold">{rec.name}</h4>
                                <p className="text-[10px] text-red-500 font-bold mt-1">¥{rec.price.toFixed(1)}</p>
                              </div>
                              <button
                                onClick={() => {
                                  onSelectRecommendedItem(rec.merchantId, rec.itemId);
                                  setIsOpen(false);
                                }}
                                className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-red-500 text-white text-[10px] font-bold hover:bg-red-600 transition"
                              >
                                <ShoppingCart size={11} />
                                <span>去加购</span>
                              </button>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}

                {isTyping && (
                  <div className="flex items-start gap-2.5">
                    <div className="w-8 h-8 rounded-lg bg-red-500/10 flex items-center justify-center text-red-500 shrink-0">
                      <Bot size={16} />
                    </div>
                    <div className={`p-3 rounded-xl rounded-tl-none text-xs ${
                      isDarkMode ? 'bg-gray-900 text-gray-300 border border-gray-800' : 'bg-gray-100 text-gray-600'
                    }`}>
                      <div className="flex items-center gap-1.5 py-1">
                        <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                        <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                        <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-bounce"></span>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Quick Prompt Chips */}
              {messages.length === 1 && (
                <div className="px-4 pb-2 shrink-0">
                  <p className="text-[10px] text-gray-400 font-extrabold uppercase tracking-wider mb-2">
                    大家都在问
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {quickQuestions.map((q, idx) => (
                      <button
                        key={idx}
                        onClick={() => handleSend(q)}
                        className={`text-[11px] font-medium px-3 py-1.5 rounded-xl border text-left cursor-pointer transition ${
                          isDarkMode 
                            ? 'bg-gray-900 border-gray-800 hover:border-red-500 text-gray-300' 
                            : 'bg-gray-50 border-gray-100 hover:border-red-500 text-gray-600'
                        }`}
                      >
                        {q}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Form Input footer */}
              <div className="p-4 border-t border-gray-100 dark:border-gray-800 shrink-0">
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    handleSend(inputValue);
                  }}
                  className="flex items-center gap-2"
                >
                  <input
                    type="text"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    placeholder="请输入您喜好的口味或想吃的菜品..."
                    className={`flex-1 text-xs px-3.5 py-2.5 rounded-xl border outline-none transition focus:border-red-500 ${
                      isDarkMode 
                        ? 'bg-gray-900 border-gray-800 text-white placeholder-gray-500' 
                        : 'bg-gray-50 border-gray-100 text-gray-900 placeholder-gray-400'
                    }`}
                  />
                  <button
                    type="submit"
                    className="p-2.5 rounded-xl bg-gradient-to-r from-red-500 to-indigo-600 text-white hover:from-red-600 hover:to-indigo-700 transition shadow-md cursor-pointer"
                  >
                    <Send size={15} />
                  </button>
                </form>
                <p className="text-[9px] text-gray-400 dark:text-gray-500 text-center mt-2 font-medium">
                  基于大模型分析 · 汇聚连山瑶壮少数民族纯正老街风味
                </p>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </>
  );
}
