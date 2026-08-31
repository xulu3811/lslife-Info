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
          <Store size={28} style={{ color: 'var(--g-blue)' }} /> 
          商家安全管控台
        </h1>
        <button className="md-btn md-btn-outline" onClick={() => setShowSettings(true)}>
          <SettingsIcon size={16} /> 入驻参数配置
        </button>
      </div>

      <div className="md-card flex flex-wrap gap-4 items-center mb-6" style={{ padding: '16px 24px' }}>
        <div className="flex-1 relative" style={{ minWidth: '300px' }}>
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }}>
            <Search size={18} />
          </div>
          <input
            type="text"
            placeholder="搜索商户名称/手机号..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="md-input w-full"
            style={{ paddingLeft: '36px' }}
          />
        </div>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="md-input"
          style={{ width: '200px' }}
        >
          <option value="">全部状态</option>
          <option value="active">正常营业</option>
          <option value="pending">待审核</option>
          <option value="offline">强制下线</option>
        </select>
      </div>

      <div className="md-card md-table-container">
        {loading ? (
          <div className="md-empty-state">加载中...</div>
        ) : merchants.length === 0 ? (
          <div className="md-empty-state">暂无商户</div>
        ) : (
          <table className="md-table">
            <thead>
              <tr>
                <th>商户名</th>
                <th>分类标签</th>
                <th>联系电话/余额</th>
                <th>行政区域 (省市区)</th>
                <th>经营状态</th>
                <th style={{ textAlign: 'right' }}>管控操作</th>
              </tr>
            </thead>
            <tbody>
              {merchants.map((merchant: any) => (
                <tr key={merchant.id}>
                  <td>
                    <div style={{ fontWeight: 500, fontSize: '15px', color: 'var(--text-primary)' }}>{merchant.name}</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>ID: {merchant.id}</div>
                  </td>
                  <td>
                    <span className="badge badge-success">
                      {merchant.category}
                    </span>
                  </td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{merchant.phone}</div>
                    <div style={{ fontSize: '13px', color: 'var(--g-red)', fontWeight: 'bold', marginTop: '4px' }}>
                      ¥ {merchant.balance || '0.00'}
                    </div>
                  </td>
                  <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {merchant.region || '-'}
                  </td>
                  <td>
                    <span className={`badge ${merchant.status === 'active' ? 'badge-success' : merchant.status === 'pending' ? 'badge-warning' : 'badge-danger'}`}>
                      {merchant.status === 'active' ? '正常营业' : merchant.status === 'pending' ? '待审核' : '强制下线'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex items-center justify-end gap-2">
                      {merchant.status === 'pending' && (
                        <button className="md-btn md-btn-primary" style={{ padding: '4px 12px' }} onClick={() => handleStatusChange(merchant.id, 'active')}>
                          <ShieldCheck size={16} /> 通过入驻
                        </button>
                      )}
                      {merchant.status === 'active' && (
                        <button className="md-btn md-btn-danger" style={{ padding: '4px 12px' }} onClick={() => handleStatusChange(merchant.id, 'offline')}>
                          <ShieldAlert size={16} /> 强制下线
                        </button>
                      )}
                      {merchant.status === 'offline' && (
                        <button className="md-btn md-btn-primary" style={{ padding: '4px 12px' }} onClick={() => handleStatusChange(merchant.id, 'active')}>
                          <ShieldCheck size={16} /> 恢复营业
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showSettings && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="md-card" style={{ width: '100%', maxWidth: '400px', padding: '24px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 500, margin: '0 0 16px 0' }}>商户入驻安全配置</h3>
            
            <div style={{ marginBottom: '24px' }}>
              <label className="flex items-center gap-3" style={{ cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={requireApproval}
                  onChange={(e) => setRequireApproval(e.target.checked)}
                  style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                />
                <div>
                  <div style={{ fontWeight: 500, color: 'var(--text-primary)' }}>开启商家入驻严格审核</div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                    开启后，所有商户在提交入驻申请后，必须由管理员后台手动审批通过才能接单。
                  </div>
                </div>
              </label>
            </div>
            
            <div className="flex justify-end gap-3" style={{ borderTop: '1px solid var(--border-color)', paddingTop: '16px' }}>
              <button className="md-btn md-btn-outline" onClick={() => setShowSettings(false)}>取消</button>
              <button className="md-btn md-btn-primary" onClick={saveSettings} disabled={savingSettings}>
                {savingSettings ? '保存中...' : '保存配置'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
