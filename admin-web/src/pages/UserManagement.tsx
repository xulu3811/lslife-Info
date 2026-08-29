import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { Search, DollarSign, Award, Users, Ban } from 'lucide-react';

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
          <Users size={28} className="text-primary" /> 
          用户管理大盘
        </h1>
      </div>

      <div className="glass-panel p-6 mb-6 flex justify-between items-center">
        <h2 className="text-lg font-medium m-0 text-secondary">全站用户检索</h2>
        <div className="relative" style={{ width: '300px' }}>
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}>
            <Search size={18} />
          </div>
          <input 
            type="text" 
            placeholder="搜索手机号或昵称" 
            className="glass-input"
            style={{ paddingLeft: '40px' }}
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          />
        </div>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : users.length === 0 ? (
          <div className="p-8 text-center text-secondary">未找到相关用户</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>用户信息</th>
                <th>身份角色</th>
                <th>发帖配额(免/付)</th>
                <th>钱包余额</th>
                <th>实名状态</th>
                <th>账号状态</th>
                <th>注册时间</th>
                <th className="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>
                    <div className="font-semibold">{user.nickname}</div>
                    <div className="text-xs text-muted mt-2 font-mono">
                      {user.phone}
                    </div>
                  </td>
                  <td>
                    <span className={`badge ${user.role === 'MERCHANT_VERIFIED' ? 'badge-success' : 'badge-neutral'}`}>
                      {user.role === 'MERCHANT_VERIFIED' ? '认证商家' : '普通用户'}
                    </span>
                  </td>
                  <td className="font-medium text-secondary">
                    {user.freeQuota} / {user.paidQuota}
                  </td>
                  <td className="font-bold text-success text-lg">
                    ￥{(user.walletBalance || 0).toFixed(2)}
                  </td>
                  <td>
                    <span className={`badge ${user.realNameStatus === 'verified' ? 'badge-success' : user.realNameStatus === 'pending' ? 'badge-warning' : 'badge-neutral'}`}>
                      {user.realNameStatus === 'verified' ? '已实名' : 
                        user.realNameStatus === 'pending' ? '待审核' : '未实名'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${user.status === 'banned' ? 'badge-danger' : 'badge-success'}`}>
                      {user.status === 'banned' ? '已封禁' : '正常'}
                    </span>
                  </td>
                  <td className="text-sm text-secondary">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="text-right">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => handleQuota(user.id, user.freeQuota || 0, user.paidQuota || 0)} className="glass-button secondary p-2" title="配额">
                        <Award size={16} className="text-primary" /> 配额
                      </button>
                      <button onClick={() => handleRecharge(user.id, user.walletBalance || 0)} className="glass-button secondary p-2" title="资金">
                        <DollarSign size={16} className="text-warning" /> 资金
                      </button>
                      <button onClick={() => handleStatus(user.id, user.status)} className="glass-button secondary p-2" title={user.status === 'banned' ? '解封' : '封禁'}>
                        <Ban size={16} className={user.status === 'banned' ? "text-success" : "text-danger"} /> {user.status === 'banned' ? '解封' : '封禁'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      
      {!loading && total > 20 && (
        <div className="flex justify-between items-center mt-4">
          <div className="text-sm text-secondary">
            共 {total} 条记录，当前第 {page} 页
          </div>
          <div className="flex gap-2">
            <button 
              className="glass-button secondary p-2 px-4" 
              disabled={page === 1}
              onClick={() => setPage(p => Math.max(1, p - 1))}
            >
              上一页
            </button>
            <button 
              className="glass-button secondary p-2 px-4" 
              disabled={page * 20 >= total}
              onClick={() => setPage(p => p + 1)}
            >
              下一页
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
