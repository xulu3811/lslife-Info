import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { Search, DollarSign, Award, Users, Ban, ShieldCheck } from 'lucide-react';

export default function UserManagement() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  const fetchUsers = () => {
    setLoading(true);
    const query = new URLSearchParams({ page: String(page), limit: '20' });
    if (search) query.append('keyword', search);
    api.get(`/admin/users?${query.toString()}`)
      .then(res => {
        setUsers(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [search, page]);

  const handleRecharge = (id: string, currentBalance: number) => {
    const amountStr = window.prompt(`当前余额: ￥${currentBalance}\n请输入要充值或扣减的金额 (扣减请输入负数):`, '0');
    if (!amountStr) return;
    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount === 0) return;

    api.put(`/admin/users/${id}/balance`, { amount })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  const handleMembership = (id: string, currentTier: string) => {
    const tiers = ['free', 'vip', 'premium'];
    const newTier = window.prompt(`当前身份: ${currentTier}\n请输入新的身份等级 (free / vip / premium):`, currentTier);
    if (!newTier || !tiers.includes(newTier) || newTier === currentTier) return;

    api.put(`/admin/users/${id}/membership`, { tier: newTier })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  const handleStatus = (id: string, currentStatus: string) => {
    const newStatus = currentStatus === 'banned' ? 'active' : 'banned';
    if (!window.confirm(`确定要${newStatus === 'banned' ? '封禁' : '解封'}该用户吗？`)) return;

    api.put(`/admin/users/${id}/status`, { status: newStatus })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  const handleQuota = (id: string, currentFree: number, currentPaid: number) => {
    const input = window.prompt(`当前配额 - 免费: ${currentFree}, 付费: ${currentPaid}\n请输入新的配额 (格式：免费数,付费数):`, `${currentFree},${currentPaid}`);
    if (!input || !input.includes(',')) return;
    const [freeStr, paidStr] = input.split(',');
    const freeQuota = parseInt(freeStr.trim());
    const paidQuota = parseInt(paidStr.trim());
    if (isNaN(freeQuota) || isNaN(paidQuota)) return;

    api.put(`/admin/users/${id}/quota`, { freeQuota, paidQuota })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <Users size={28} style={{ color: 'var(--g-blue)' }} /> 
          用户管理大盘
        </h1>
      </div>

      <div className="md-card flex flex-wrap justify-between items-center mb-6" style={{ padding: '16px 24px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 500, margin: 0, color: 'var(--text-secondary)' }}>全站用户检索</h2>
        <div className="relative" style={{ width: '300px' }}>
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }}>
            <Search size={18} />
          </div>
          <input 
            type="text" 
            placeholder="搜索手机号或昵称" 
            className="md-input w-full"
            style={{ paddingLeft: '36px' }}
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          />
        </div>
      </div>

      <div className="md-card md-table-container">
        {loading ? (
          <div className="md-empty-state">加载中...</div>
        ) : users.length === 0 ? (
          <div className="md-empty-state">未找到相关用户</div>
        ) : (
          <table className="md-table">
            <thead>
              <tr>
                <th>用户信息</th>
                <th>身份角色</th>
                <th>发帖配额(免/付)</th>
                <th>钱包余额</th>
                <th>实名状态</th>
                <th>账号状态</th>
                <th>注册时间</th>
                <th style={{ textAlign: 'right' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>
                    <div style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{user.nickname}</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px', fontFamily: 'monospace' }}>
                      {user.phone}
                    </div>
                  </td>
                  <td>
                    <span className={`badge ${user.role === 'MERCHANT_VERIFIED' ? 'badge-success' : 'badge-danger'}`}>
                      {user.role === 'MERCHANT_VERIFIED' ? '认证商家' : '普通用户'}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontWeight: 500 }}>
                      {user.freeQuota} / {user.paidQuota}
                    </span>
                  </td>
                  <td>
                    <span style={{ color: 'var(--g-green)', fontWeight: 'bold' }}>
                      ¥ {(user.walletBalance || 0).toFixed(2)}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${user.realNameStatus === 'verified' ? 'badge-success' : 'badge-danger'}`}>
                      {user.realNameStatus === 'verified' ? '已实名' : '未实名'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${user.status === 'active' ? 'badge-success' : 'badge-danger'}`}>
                      {user.status === 'active' ? '正常' : '已封禁'}
                    </span>
                  </td>
                  <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex items-center justify-end gap-2">
                      <button className="md-btn-icon" onClick={() => handleQuota(user.id, user.freeQuota || 0, user.paidQuota || 0)} title="修改配额">
                        <Award size={16} />
                      </button>
                      <button className="md-btn-icon" style={{ color: 'var(--g-yellow)' }} onClick={() => handleRecharge(user.id, user.walletBalance || 0)} title="充值/扣款">
                        <DollarSign size={16} />
                      </button>
                      <button className="md-btn-icon" style={{ color: user.status === 'banned' ? 'var(--g-green)' : 'var(--g-red)' }} onClick={() => handleStatus(user.id, user.status)} title={user.status === 'banned' ? '解封' : '封禁'}>
                        {user.status === 'banned' ? <ShieldCheck size={16} /> : <Ban size={16} />}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!loading && total > 20 && (
           <div className="flex justify-between items-center mt-6 p-4 pt-0">
             <button disabled={page === 1} onClick={() => setPage(p => p - 1)} className="md-btn md-btn-outline">上一页</button>
             <span className="text-secondary font-medium" style={{ fontSize: '13px' }}>第 {page} 页 / 共 {Math.ceil(total / 20)} 页</span>
             <button disabled={page * 20 >= total} onClick={() => setPage(p => p + 1)} className="md-btn md-btn-outline">下一页</button>
           </div>
        )}
      </div>
    </div>
  );
}
