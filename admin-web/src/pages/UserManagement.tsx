import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { Search, DollarSign, Award, Users, Ban } from 'lucide-react';

export default function UserManagement() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchUsers = () => {
    setLoading(true);
    api.get(`/admin/users${search ? `?search=${encodeURIComponent(search)}` : ''}`)
      .then(res => setUsers(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [search]);

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
            onChange={(e) => setSearch(e.target.value)}
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
                <th>手机号</th>
                <th>会员等级</th>
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
                    <div className="text-xs text-muted mt-2 font-mono">ID: {user.id.slice(0, 8)}...</div>
                  </td>
                  <td className="font-medium text-secondary">{user.phone}</td>
                  <td>
                    <span className={`badge ${user.membershipTier === 'free' ? 'badge-neutral' : user.membershipTier === 'vip' ? 'badge-warning' : 'badge-danger'}`}>
                      {user.membershipTier.toUpperCase()}
                    </span>
                  </td>
                  <td className="font-bold text-success text-lg">
                    ￥{user.walletBalance.toFixed(2)}
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
                      <button onClick={() => handleRecharge(user.id, user.walletBalance)} className="glass-button secondary p-2" title="调额">
                        <DollarSign size={16} className="text-primary" /> 调额
                      </button>
                      <button onClick={() => handleMembership(user.id, user.membershipTier)} className="glass-button secondary p-2" title="身份">
                        <Award size={16} className="text-warning" /> 身份
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
    </div>
  );
}
