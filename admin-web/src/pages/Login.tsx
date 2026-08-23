import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, User, KeyRound } from 'lucide-react';
import api from '../utils/axios';
import '../index.css';

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totp, setTotp] = useState('');
  const [showMfa, setShowMfa] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) return;
    
    // In phase 1, we simulate triggering MFA
    if (!showMfa) {
      setShowMfa(true);
      return;
    }

    setLoading(true);
    try {
      const res = await api.post('/admin/login', { username, password });
      localStorage.setItem('admin_token', res.data.data?.token || 'mock_token');
      navigate('/dashboard');
    } catch (err: any) {
      alert(err.response?.data?.message || '登录失败');
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen p-4">
      <div className="glass-panel p-8 w-full" style={{ maxWidth: '440px' }}>
        <div className="text-center mb-8">
          <div className="inline-flex mb-4">
            <img src="/favicon.png" alt="LsLife Logo" style={{ width: '72px', height: '72px', objectFit: 'contain', filter: 'drop-shadow(0 8px 16px rgba(229, 57, 53, 0.25))' }} />
          </div>
          <h1 className="text-gradient text-3xl mb-2">LsLife Admin</h1>
          <p className="m-0 text-secondary font-medium">系统管理与安全控制台</p>
        </div>

        <form onSubmit={handleLogin} className="flex-col gap-6">
          {!showMfa ? (
            <>
              <div>
                <label className="block mb-2 text-sm text-secondary font-medium">管理账号</label>
                <div className="relative">
                  <div style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }}>
                    <User size={18} />
                  </div>
                  <input
                    type="text"
                    className="glass-input w-full"
                    style={{ paddingLeft: '44px', paddingTop: '12px', paddingBottom: '12px' }}
                    placeholder="输入管理员账号"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                </div>
              </div>
              
              <div>
                <label className="block mb-2 text-sm text-secondary font-medium">访问密码</label>
                <div className="relative">
                  <div style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }}>
                    <Lock size={18} />
                  </div>
                  <input
                    type="password"
                    className="glass-input w-full"
                    style={{ paddingLeft: '44px', paddingTop: '12px', paddingBottom: '12px' }}
                    placeholder="输入管理密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
              </div>
            </>
          ) : (
            <div className="animate-fade-in">
              <label className="block mb-2 text-sm text-secondary font-medium">两步验证 (2FA) 代码</label>
              <div className="relative">
                <div style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }}>
                  <KeyRound size={18} />
                </div>
                <input
                  type="text"
                  className="glass-input w-full text-center"
                  style={{ paddingLeft: '44px', letterSpacing: '8px', fontSize: '20px', fontWeight: 'bold' }}
                  placeholder="000000"
                  maxLength={6}
                  value={totp}
                  onChange={(e) => setTotp(e.target.value)}
                  autoFocus
                />
              </div>
              <p className="text-xs text-secondary mt-3 text-center">
                请打开您的 Authenticator 应用获取 6 位安全验证码
              </p>
            </div>
          )}

          <button 
            type="submit" 
            className="glass-button w-full mt-2" 
            style={{ padding: '14px' }}
            disabled={loading}
          >
            {loading ? '身份校验中...' : (showMfa ? '安全登录' : '下一步 (2FA校验)')}
          </button>
        </form>
      </div>
    </div>
  );
}
