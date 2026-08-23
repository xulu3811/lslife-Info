import { useEffect, useState } from 'react';
import { Store, Search, ShieldAlert, ShieldCheck, Settings as SettingsIcon } from 'lucide-react';
import api from '../utils/axios';

interface Merchant {
  id: string;
  name: string;
  phone: string;
  status: string;
  sales: number;
  rating: number;
  category: string;
  createdAt: string;
}

export function MerchantManagement() {
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  
  // Settings modal state
  const [showSettings, setShowSettings] = useState(false);
  const [requireApproval, setRequireApproval] = useState(false);
  const [savingSettings, setSavingSettings] = useState(false);

  const fetchMerchants = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/merchants', { params: { search, status } });
      setMerchants(res.data.data?.list || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchSettings = async () => {
    try {
      const res = await api.get('/admin/settings');
      setRequireApproval(res.data.data?.merchant_require_approval === 'true');
    } catch (e) {
      console.error('Failed to fetch settings', e);
    }
  };

  useEffect(() => {
    fetchMerchants();
  }, [search, status]);

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!window.confirm(`确认将商户状态修改为 ${newStatus}?`)) return;
    try {
      await api.post(`/admin/merchants/${id}/status`, { status: newStatus });
      fetchMerchants();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  const saveSettings = async () => {
    setSavingSettings(true);
    try {
      await api.put('/admin/settings', {
        key: 'merchant_require_approval',
        value: requireApproval ? 'true' : 'false'
      });
      setShowSettings(false);
      alert('设置已保存');
    } catch (e: any) {
      alert('保存失败');
    } finally {
      setSavingSettings(false);
    }
  };

  return (
    <div className="flex-col gap-6 relative">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3 m-0">
          <Store size={28} className="text-primary" /> 
          商家安全管控台
        </h1>
        <button className="glass-button secondary" onClick={() => setShowSettings(true)}>
          <SettingsIcon size={18} className="text-secondary" /> 入驻参数配置
        </button>
      </div>

      <div className="glass-panel p-6 flex flex-wrap gap-4 items-center mb-6">
        <div className="flex-1 relative">
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}>
            <Search size={18} />
          </div>
          <input
            type="text"
            placeholder="搜索商户名称/手机号..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="glass-input"
            style={{ paddingLeft: '40px' }}
          />
        </div>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="glass-input"
          style={{ width: '200px' }}
        >
          <option value="">全部状态</option>
          <option value="active">正常营业</option>
          <option value="pending">待审核</option>
          <option value="offline">强制下线</option>
        </select>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>商户名</th>
                <th>分类标签</th>
                <th>联系电话</th>
                <th>评分/销量大盘</th>
                <th>经营状态</th>
                <th className="text-right">管控操作</th>
              </tr>
            </thead>
            <tbody>
              {merchants.map((merchant) => (
                <tr key={merchant.id}>
                  <td>
                    <div className="font-semibold text-base mb-1">{merchant.name}</div>
                    <div className="text-xs text-secondary">
                      入驻时间: {new Date(merchant.createdAt).toLocaleDateString()}
                    </div>
                  </td>
                  <td>
                    <span className="badge badge-neutral">{merchant.category || '默认分类'}</span>
                  </td>
                  <td className="font-medium text-secondary">{merchant.phone}</td>
                  <td>
                    <div className="text-warning font-bold text-lg mb-1">★ {merchant.rating.toFixed(1)}</div>
                    <div className="text-xs text-secondary">已售 {merchant.sales} 单</div>
                  </td>
                  <td>
                    <span className={`badge ${merchant.status === 'active' ? 'badge-success' : merchant.status === 'pending' ? 'badge-warning' : 'badge-danger'}`}>
                      {merchant.status === 'active' ? '正常营业' : merchant.status === 'pending' ? '待审核' : '已下线'}
                    </span>
                  </td>
                  <td className="text-right">
                    <div className="flex gap-2 justify-end">
                      {merchant.status === 'pending' && (
                        <>
                          <button className="glass-button success p-2 px-3 text-sm" onClick={() => handleStatusChange(merchant.id, 'active')}>
                            <ShieldCheck size={16} /> 准入
                          </button>
                          <button className="glass-button danger p-2 px-3 text-sm" onClick={() => handleStatusChange(merchant.id, 'offline')}>
                            <ShieldAlert size={16} /> 驳回
                          </button>
                        </>
                      )}
                      {merchant.status === 'active' && (
                        <button className="glass-button danger p-2 px-3 text-sm" onClick={() => handleStatusChange(merchant.id, 'offline')} title="强制下线">
                          <ShieldAlert size={16} /> 下线
                        </button>
                      )}
                      {merchant.status === 'offline' && (
                        <button className="glass-button success p-2 px-3 text-sm" onClick={() => handleStatusChange(merchant.id, 'active')} title="恢复营业">
                          <ShieldCheck size={16} /> 恢复
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {merchants.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-muted">暂无商户</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Settings Modal */}
      {showSettings && (
        <div className="flex items-center justify-center" style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(15, 23, 42, 0.4)', backdropFilter: 'blur(4px)', zIndex: 100
        }}>
          <div className="glass-panel p-8" style={{ width: 440 }}>
            <h2 className="text-xl font-bold mb-6 m-0">商户入驻参数配置</h2>
            
            <div className="flex items-center gap-3 mb-4 p-4" style={{ background: 'rgba(0,0,0,0.03)', borderRadius: 'var(--radius-md)' }}>
              <input 
                type="checkbox" 
                id="requireApproval"
                checked={requireApproval}
                onChange={(e) => setRequireApproval(e.target.checked)}
                style={{ width: 20, height: 20, accentColor: 'var(--primary)' }}
              />
              <label htmlFor="requireApproval" className="font-medium cursor-pointer text-base">
                新商户入驻需要人工二次审核
              </label>
            </div>
            <p className="text-sm text-secondary mb-8">
              开启后，APP 端新注册的商户主体状态将默认为"待审核"，需管理员在此后台手动点击「准入」后方可发布商品和营业。
            </p>
            
            <div className="flex gap-4 justify-end">
              <button 
                className="glass-button secondary"
                onClick={() => setShowSettings(false)}
              >
                取消
              </button>
              <button className="glass-button" onClick={saveSettings} disabled={savingSettings}>
                {savingSettings ? '保存中...' : '确认应用策略'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
