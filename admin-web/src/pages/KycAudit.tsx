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
      .then(res => setUsers(Array.isArray(res.data.data) ? res.data.data : []))
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
          <UserCheck size={28} style={{ color: 'var(--g-blue)' }} /> 
          实名认证 (KYC) 审核大盘
        </h1>
      </div>

      <div className="md-card">
        <div className="flex gap-6 mb-6" style={{ borderBottom: '1px solid var(--border-color)' }}>
          {tabs.map(t => (
            <div 
              key={t.id}
              onClick={() => setTab(t.id as any)}
              style={{
                paddingBottom: '12px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                fontWeight: tab === t.id ? 600 : 500,
                color: tab === t.id ? 'var(--g-blue)' : 'var(--text-secondary)',
                borderBottom: tab === t.id ? '2px solid var(--g-blue)' : '2px solid transparent',
                marginBottom: '-1px'
              }}
            >
              {t.label}
            </div>
          ))}
        </div>
        
        {loading ? (
          <div className="md-empty-state">加载中...</div>
        ) : users.length === 0 ? (
          <div className="md-empty-state">
            {tab === 'pending' ? '暂无需要审核的实名认证。' : '暂无相关记录。'}
          </div>
        ) : (
          <div className="flex-col gap-4">
            {users.map(user => (
              <div key={user.id} style={{ padding: '20px', border: '1px solid var(--border-color)', borderRadius: '12px', background: 'var(--surface-hover)' }}>
                <div className="flex justify-between mb-4">
                  <div className="flex gap-3 items-center">
                    <span style={{ fontWeight: 600, fontSize: '18px', color: 'var(--text-primary)' }}>{user.realName || '未知姓名'}</span>
                    <span className="badge badge-success" style={{ fontFamily: 'monospace' }}>{user.phone}</span>
                  </div>
                  <div className="flex gap-2">
                    {tab === 'pending' && (
                      <>
                        <button onClick={() => handleAudit(user.id, 'approve')} className="md-btn md-btn-primary">
                          <CheckCircle size={16} /> 通过
                        </button>
                        <button onClick={() => handleAudit(user.id, 'reject')} className="md-btn md-btn-danger">
                          <XCircle size={16} /> 驳回
                        </button>
                      </>
                    )}
                  </div>
                </div>
                
                <div className="flex-col gap-2" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <div>用户昵称: <span style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{user.nickname}</span></div>
                  <div>身份证哈希: <span style={{ fontFamily: 'monospace' }}>{user.idCardHash || '无'}</span></div>
                  <div>最近更新: {new Date(user.updatedAt).toLocaleString()}</div>
                </div>
                
                {tab === 'pending' && (
                  <div className="mt-4 flex gap-4 overflow-x-auto pb-2">
                    {user.idCardFrontImage && (
                      <div className="flex-col gap-1 items-center">
                        <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text-secondary)' }}>正面照</span>
                        <img src={user.idCardFrontImage} alt="正面照" style={{ height: '128px', objectFit: 'contain', background: 'var(--surface)', borderRadius: '8px', padding: '4px', border: '1px dashed var(--border-color)' }} />
                      </div>
                    )}
                    {user.idCardBackImage && (
                      <div className="flex-col gap-1 items-center">
                        <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text-secondary)' }}>反面照</span>
                        <img src={user.idCardBackImage} alt="反面照" style={{ height: '128px', objectFit: 'contain', background: 'var(--surface)', borderRadius: '8px', padding: '4px', border: '1px dashed var(--border-color)' }} />
                      </div>
                    )}
                    {user.idCardHandheldImage && (
                      <div className="flex-col gap-1 items-center">
                        <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text-secondary)' }}>手持照</span>
                        <img src={user.idCardHandheldImage} alt="手持照" style={{ height: '128px', objectFit: 'contain', background: 'var(--surface)', borderRadius: '8px', padding: '4px', border: '1px dashed var(--border-color)' }} />
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
