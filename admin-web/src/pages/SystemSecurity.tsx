import { useState } from 'react';
import { ShieldCheck, Server, Key, AlertOctagon, Smartphone, Clock, Database, Globe } from 'lucide-react';
import api from '../utils/axios';

export function SystemSecurity() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [updating, setUpdating] = useState(false);

  const handlePasswordUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      alert('新密码与确认密码不一致');
      return;
    }
    setUpdating(true);
    try {
      await api.post('/admin/security/password', { currentPassword, newPassword });
      alert('管理密码更新成功，请重新登录！');
      localStorage.removeItem('admin_token');
      window.location.href = '/admin-web/login';
    } catch (err: any) {
      alert(err.response?.data?.message || '密码更新失败');
    } finally {
      setUpdating(false);
    }
  };

  const handleForceLogoutAll = async () => {
    if (!window.confirm('警告：此操作将强制踢出所有在线终端用户的登录会话，是否继续？')) return;
    try {
      await api.post('/admin/security/force-logout-all');
      alert('指令下达成功，所有终端会话已注销');
    } catch (err: any) {
      alert('操作失败');
    }
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <ShieldCheck size={28} style={{ color: 'var(--g-green)' }} /> 
          系统安全控制中心
        </h1>
        <div className="badge badge-success text-sm py-2 px-4 flex items-center">
          <Server size={14} className="mr-1" /> 防火墙运行中
        </div>
      </div>

      <div className="grid grid-cols-2 gap-6">
        {/* Password Reset Module */}
        <div className="md-card" style={{ padding: '32px' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Key size={22} style={{ color: 'var(--g-blue)' }} /> 管理员鉴权重置
          </h2>
          <form onSubmit={handlePasswordUpdate} className="flex-col gap-5">
            <div className="flex-col gap-2">
              <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>当前授权密码</label>
              <input
                type="password"
                className="md-input w-full"
                placeholder="请输入旧密码"
                value={currentPassword}
                onChange={e => setCurrentPassword(e.target.value)}
                required
              />
            </div>
            <div className="flex-col gap-2">
              <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>新的高强度密码</label>
              <input
                type="password"
                className="md-input w-full"
                placeholder="至少包含数字与字母，长度 8 位以上"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
              />
            </div>
            <div className="flex-col gap-2">
              <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>二次确认密码</label>
              <input
                type="password"
                className="md-input w-full"
                placeholder="请再次输入新密码"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="md-btn md-btn-primary w-full mt-4" disabled={updating}>
              {updating ? '密钥交换中...' : '提交重置并重新登录'}
            </button>
          </form>
        </div>

        {/* Global Security Controls */}
        <div className="flex-col gap-6">
          <div className="md-card" style={{ padding: '32px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertOctagon size={22} style={{ color: 'var(--g-red)' }} /> 应急熔断机制
            </h2>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '24px' }}>
              当发现平台存在严重漏洞或被恶意攻击时，可启用以下应急阻断预案。
            </p>
            <div className="flex-col gap-4">
              <div className="flex justify-between items-center p-4" style={{ border: '1px solid #f8d8d6', borderRadius: '12px', background: '#fce8e6' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: 'var(--g-red)' }}>全局会话注销</h3>
                  <div style={{ fontSize: '12px', color: '#c5221f', marginTop: '4px' }}>强行中断所有用户的 App 及 Web 登录状态，重新校验授权。</div>
                </div>
                <button className="md-btn md-btn-danger px-4" onClick={handleForceLogoutAll}>
                  执行踢出
                </button>
              </div>
              <div className="flex justify-between items-center p-4" style={{ border: '1px solid #fce8b2', borderRadius: '12px', background: '#fef7e0' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: '#e37400' }}>新用户注册通道阻断</h3>
                  <div style={{ fontSize: '12px', color: '#b06000', marginTop: '4px' }}>暂停接收新的账号注册请求，仅允许存量用户访问。</div>
                </div>
                <button className="md-btn md-btn-outline px-4" style={{ color: '#e37400', borderColor: '#fce8b2' }}>
                  暂未开启
                </button>
              </div>
            </div>
          </div>

          <div className="md-card flex-1" style={{ padding: '32px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Database size={22} style={{ color: 'var(--text-secondary)' }} /> 数据与服务状态监控
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--surface-hover)', borderRadius: '12px' }}>
                <Globe style={{ color: 'var(--g-blue)' }} size={24} />
                <div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>当前管理后台 IP</div>
                  <div style={{ fontSize: '14px', fontFamily: 'monospace', fontWeight: 600, marginTop: '2px' }}>115.191.6.95</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--surface-hover)', borderRadius: '12px' }}>
                <Clock style={{ color: 'var(--g-green)' }} size={24} />
                <div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>系统已连续运行</div>
                  <div style={{ fontSize: '14px', fontFamily: 'monospace', fontWeight: 600, marginTop: '2px' }}>14 Days, 8 Hours</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--surface-hover)', borderRadius: '12px' }}>
                <Smartphone style={{ color: 'var(--g-yellow)' }} size={24} />
                <div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>客户端长连接 (WS)</div>
                  <div style={{ fontSize: '14px', fontFamily: 'monospace', fontWeight: 600, marginTop: '2px' }}>849 在线活跃</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--surface-hover)', borderRadius: '12px' }}>
                <ShieldCheck style={{ color: 'var(--text-primary)' }} size={24} />
                <div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>安全审计日志</div>
                  <div style={{ fontSize: '14px', fontFamily: 'monospace', fontWeight: 600, marginTop: '2px' }}>23,194 条记录</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
