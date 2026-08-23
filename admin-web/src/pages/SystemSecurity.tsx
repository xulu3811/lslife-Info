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
          <ShieldCheck size={28} className="text-success" /> 
          系统安全控制中心
        </h1>
        <div className="badge badge-success text-sm py-2 px-4">
          <Server size={14} className="mr-1" /> 防火墙运行中
        </div>
      </div>

      <div className="grid grid-cols-2 gap-6">
        {/* Password Reset Module */}
        <div className="glass-panel p-8">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-2 m-0 text-primary">
            <Key size={22} /> 管理员鉴权重置
          </h2>
          <form onSubmit={handlePasswordUpdate} className="flex-col gap-5">
            <div>
              <label className="block text-sm font-medium text-secondary mb-2">当前授权密码</label>
              <input
                type="password"
                className="glass-input"
                placeholder="请输入旧密码"
                value={currentPassword}
                onChange={e => setCurrentPassword(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-secondary mb-2">新的高强度密码</label>
              <input
                type="password"
                className="glass-input"
                placeholder="至少包含数字与字母，长度 8 位以上"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-secondary mb-2">二次确认密码</label>
              <input
                type="password"
                className="glass-input"
                placeholder="请再次输入新密码"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="glass-button w-full mt-4" disabled={updating}>
              {updating ? '密钥交换中...' : '提交重置并重新登录'}
            </button>
          </form>
        </div>

        {/* Global Security Controls */}
        <div className="flex-col gap-6">
          <div className="glass-panel p-8">
            <h2 className="text-xl font-bold mb-6 flex items-center gap-2 m-0 text-danger">
              <AlertOctagon size={22} /> 应急熔断机制
            </h2>
            <p className="text-sm text-secondary mb-6">
              当发现平台存在严重漏洞或被恶意攻击时，可启用以下应急阻断预案。
            </p>
            <div className="flex-col gap-4">
              <div className="flex justify-between items-center p-4" style={{ border: '1px solid var(--danger-bg)', borderRadius: 'var(--radius-md)', background: 'rgba(239, 68, 68, 0.05)' }}>
                <div>
                  <h3 className="m-0 text-base font-semibold text-danger mb-1">全局会话注销</h3>
                  <div className="text-xs text-secondary">强行中断所有用户的 App 及 Web 登录状态，重新校验授权。</div>
                </div>
                <button className="glass-button danger px-4" onClick={handleForceLogoutAll}>
                  执行踢出
                </button>
              </div>
              <div className="flex justify-between items-center p-4" style={{ border: '1px solid var(--warning-bg)', borderRadius: 'var(--radius-md)', background: 'rgba(245, 158, 11, 0.05)' }}>
                <div>
                  <h3 className="m-0 text-base font-semibold text-warning mb-1">新用户注册通道阻断</h3>
                  <div className="text-xs text-secondary">暂停接收新的账号注册请求，仅允许存量用户访问。</div>
                </div>
                <button className="glass-button secondary text-warning px-4">
                  暂未开启
                </button>
              </div>
            </div>
          </div>

          <div className="glass-panel p-8 flex-1">
            <h2 className="text-xl font-bold mb-6 flex items-center gap-2 m-0 text-info">
              <Database size={22} /> 数据与服务状态监控
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--bg-color)', borderRadius: 'var(--radius-md)' }}>
                <Globe className="text-primary" size={24} />
                <div>
                  <div className="text-xs text-secondary mb-1">当前管理后台 IP</div>
                  <div className="text-sm font-mono font-semibold">115.191.6.95</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--bg-color)', borderRadius: 'var(--radius-md)' }}>
                <Clock className="text-info" size={24} />
                <div>
                  <div className="text-xs text-secondary mb-1">系统已连续运行</div>
                  <div className="text-sm font-mono font-semibold">14 Days, 8 Hours</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--bg-color)', borderRadius: 'var(--radius-md)' }}>
                <Smartphone className="text-success" size={24} />
                <div>
                  <div className="text-xs text-secondary mb-1">客户端长连接 (WS)</div>
                  <div className="text-sm font-mono font-semibold">849 在线活跃</div>
                </div>
              </div>
              <div className="p-4 flex items-center gap-3" style={{ background: 'var(--bg-color)', borderRadius: 'var(--radius-md)' }}>
                <ShieldCheck className="text-warning" size={24} />
                <div>
                  <div className="text-xs text-secondary mb-1">安全审计日志</div>
                  <div className="text-sm font-mono font-semibold">23,194 条记录</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
