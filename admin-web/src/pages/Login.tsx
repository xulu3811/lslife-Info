import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, User, KeyRound, ShieldCheck } from 'lucide-react';
import api from '../utils/axios';

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
      navigate('/admin-web/dashboard');
    } catch (err: any) {
      alert(err.response?.data?.message || '登录失败');
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen" style={{ background: 'var(--surface-hover)' }}>
      <div className="md-card flex-col items-center" style={{ width: '100%', maxWidth: '440px', padding: '48px 40px' }}>
        
        <div className="text-center mb-10 flex-col items-center">
          <div className="inline-flex mb-6" style={{ background: '#fff', borderRadius: '24px', padding: '16px', boxShadow: '0 8px 24px rgba(0,0,0,0.06)' }}>
            <img src="/admin-web/logo.png" alt="qylife Logo" style={{ width: '64px', height: '64px', objectFit: 'contain' }} />
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)', margin: '0 0 8px 0' }}>qylife Admin</h1>
          <p style={{ margin: 0, fontSize: '14px', color: 'var(--text-secondary)', fontWeight: 500 }}>清远同城 - 系统管理与安全控制台</p>
        </div>

        <form onSubmit={handleLogin} className="flex-col gap-5 w-full">
          {!showMfa ? (
            <>
              <div className="flex-col gap-2">
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>管理账号</label>
                <div className="relative flex items-center">
                  <div style={{ position: 'absolute', left: '16px', color: 'var(--text-secondary)' }}>
                    <User size={18} />
                  </div>
                  <input
                    type="text"
                    className="md-input w-full"
                    style={{ paddingLeft: '44px', paddingTop: '12px', paddingBottom: '12px' }}
                    placeholder="输入管理员账号"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                </div>
              </div>
              
              <div className="flex-col gap-2">
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>访问密码</label>
                <div className="relative flex items-center">
                  <div style={{ position: 'absolute', left: '16px', color: 'var(--text-secondary)' }}>
                    <Lock size={18} />
                  </div>
                  <input
                    type="password"
                    className="md-input w-full"
                    style={{ paddingLeft: '44px', paddingTop: '12px', paddingBottom: '12px' }}
                    placeholder="输入管理密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
              </div>
            </>
          ) : (
            <div style={{ animation: 'fadeIn 0.3s ease' }}>
              <div className="flex items-center justify-between mb-2">
                <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>两步验证 (2FA) 代码</label>
                <ShieldCheck size={16} style={{ color: 'var(--g-green)' }} />
              </div>
              <div className="relative flex items-center">
                <div style={{ position: 'absolute', left: '16px', color: 'var(--text-secondary)' }}>
                  <KeyRound size={18} />
                </div>
                <input
                  type="text"
                  className="md-input w-full"
                  style={{ paddingLeft: '44px', paddingTop: '16px', paddingBottom: '16px', letterSpacing: '8px', fontSize: '24px', fontWeight: 700, textAlign: 'center' }}
                  placeholder="000000"
                  maxLength={6}
                  value={totp}
                  onChange={(e) => setTotp(e.target.value)}
                  autoFocus
                />
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '12px', textAlign: 'center' }}>
                请打开您的 Authenticator 应用获取 6 位安全验证码
              </p>
            </div>
          )}

          <button 
            type="submit" 
            className="md-btn md-btn-primary w-full mt-4" 
            style={{ padding: '14px', fontSize: '15px' }}
            disabled={loading}
          >
            {loading ? '身份校验中...' : (showMfa ? '安全登录' : '下一步 (2FA校验)')}
          </button>
        </form>
      </div>
    </div>
  );
}
