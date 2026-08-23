import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { CheckCircle, XCircle, UserCheck } from 'lucide-react';

export default function KycAudit() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<'pending' | 'verified' | 'none'>('pending');

  const fetchUsers = () => {
    setLoading(true);
    api.get(`/admin/kyc?status=${tab}`)
      .then(res => setUsers(res.data.data?.list || []))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [tab]);

  const handleAudit = (id: string, action: 'approve' | 'reject') => {
    if (!window.confirm(`确定要${action === 'approve' ? '通过' : '驳回'}该实名认证吗？`)) return;
    
    api.post(`/admin/kyc/${id}/audit`, { action })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  const tabs = [
    { id: 'pending', label: '待审核' },
    { id: 'verified', label: '已通过' },
    { id: 'none', label: '未实名/已驳回' }
  ];

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <UserCheck size={28} className="text-primary" /> 
          实名认证 (KYC) 审核大盘
        </h1>
      </div>

      <div className="glass-panel p-6">
        <div className="flex gap-6 mb-6" style={{ borderBottom: '1px solid var(--surface-border)' }}>
          {tabs.map(t => (
            <div 
              key={t.id}
              onClick={() => setTab(t.id as any)}
              className={`pb-3 cursor-pointer transition-all duration-200 ${tab === t.id ? 'text-primary font-semibold' : 'text-secondary font-medium'}`}
              style={{ borderBottom: tab === t.id ? '2px solid var(--primary)' : '2px solid transparent' }}
            >
              {t.label}
            </div>
          ))}
        </div>
        
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : users.length === 0 ? (
          <div className="p-8 text-center text-secondary">
            {tab === 'pending' ? '暂无需要审核的实名认证。' : '暂无相关记录。'}
          </div>
        ) : (
          <div className="flex-col gap-4">
            {users.map(user => (
              <div key={user.id} className="p-4" style={{ border: '1px solid var(--surface-border)', borderRadius: 'var(--radius-md)', background: 'rgba(255,255,255,0.3)' }}>
                <div className="flex justify-between mb-4">
                  <div className="flex gap-3 items-center">
                    <span className="font-semibold text-lg">{user.realName || '未知姓名'}</span>
                    <span className="badge badge-info font-mono">{user.phone}</span>
                  </div>
                  <div className="flex gap-2">
                    {tab === 'pending' && (
                      <>
                        <button onClick={() => handleAudit(user.id, 'approve')} className="glass-button success p-2 px-3">
                          <CheckCircle size={16} /> 通过
                        </button>
                        <button onClick={() => handleAudit(user.id, 'reject')} className="glass-button danger p-2 px-3">
                          <XCircle size={16} /> 驳回
                        </button>
                      </>
                    )}
                  </div>
                </div>
                
                <div className="text-sm text-secondary flex-col gap-2">
                  <span>用户昵称: <span className="font-medium text-primary">{user.nickname}</span></span>
                  <span>身份证哈希: <span className="font-mono text-muted">{user.idCardHash || '无'}</span></span>
                  <span>最近更新: {new Date(user.updatedAt).toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
