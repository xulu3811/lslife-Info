import { useEffect, useState } from 'react';
import { Users, FileText, Activity, AlertTriangle } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import api from '../utils/axios';
import { Link } from 'react-router-dom';

const StatCard = ({ title, value, trend, icon: Icon, color, linkTo }: any) => (
  <div className="glass-panel p-6 flex-col gap-4">
    <div className="flex justify-between items-start">
      <div>
        <p className="text-secondary text-sm m-0 mb-2">{title}</p>
        <h3 className="text-3xl font-bold m-0">{value}</h3>
      </div>
      <div className="p-3" style={{ background: `rgba(${color}, 0.15)`, borderRadius: '12px', color: `rgb(${color})` }}>
        <Icon size={24} />
      </div>
    </div>
    <div className="flex justify-between items-center mt-2">
      <div>
        <span className={trend >= 0 ? 'text-success font-semibold text-sm' : 'text-danger font-semibold text-sm'}>
          {trend >= 0 ? '+' : ''}{trend}%
        </span>
        <span className="text-secondary text-sm ml-2">较昨日</span>
      </div>
      {linkTo && (
        <Link to={linkTo} className="text-primary text-sm font-medium" style={{ textDecoration: 'none' }}>
          前往管理 &rarr;
        </Link>
      )}
    </div>
  </div>
);

export default function Dashboard() {
  const [stats, setStats] = useState({
    newUsers: 0,
    todayPosts: 0,
    revenue: 0,
    pendingReviews: 0,
    trendData: [],
  });

  useEffect(() => {
    api.get('/admin/dashboard').then(res => {
      if (res.data?.data) {
        setStats(res.data.data);
      }
    }).catch(console.error);
  }, []);

  return (
    <div className="flex-col gap-6">
      <div className="grid grid-cols-4 gap-6">
        <StatCard title="今日新增用户" value={(stats?.newUsers || 0).toString()} trend={12.5} icon={Users} color="229, 57, 53" linkTo="/users" />
        <StatCard title="今日发帖量" value={(stats?.todayPosts || 0).toString()} trend={8.2} icon={FileText} color="167, 139, 250" linkTo="/content" />
        <StatCard title="平台充值流水 (元)" value={`￥${stats?.revenue || 0}`} trend={-2.4} icon={Activity} color="16, 185, 129" />
        <StatCard title="待办审批总数" value={(stats?.pendingReviews || 0).toString()} trend={10} icon={AlertTriangle} color="239, 68, 68" linkTo="/content" />
      </div>

      <div className="grid gap-6" style={{ gridTemplateColumns: '2fr 1fr' }}>
        <div className="glass-panel p-6" style={{ minHeight: '400px' }}>
          <h3 className="text-lg font-semibold m-0 mb-6">平台核心指标趋势 (7日快照)</h3>
          <div className="h-full" style={{ minHeight: '300px' }}>
            {stats.trendData && stats.trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={stats.trendData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                  <Line type="monotone" name="新增用户" dataKey="users" stroke="#e53935" strokeWidth={2} activeDot={{ r: 6 }} />
                  <Line type="monotone" name="发帖量" dataKey="posts" stroke="#8b5cf6" strokeWidth={2} />
                  <CartesianGrid stroke="#ccc" strokeDasharray="5 5" opacity={0.2} />
                  <XAxis dataKey="date" stroke="var(--secondary-text)" tick={{ fontSize: 12 }} />
                  <YAxis yAxisId="left" stroke="var(--secondary-text)" tick={{ fontSize: 12 }} />
                  <YAxis yAxisId="right" orientation="right" stroke="var(--secondary-text)" tick={{ fontSize: 12 }} />
                  <Tooltip contentStyle={{ backgroundColor: 'var(--panel-bg)', borderColor: 'var(--surface-border)', borderRadius: '8px' }} />
                  <Legend verticalAlign="top" height={36} />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center text-secondary h-full" style={{ border: '1px dashed var(--surface-border)', borderRadius: '8px', minHeight: '300px' }}>
                图表数据加载中...
              </div>
            )}
          </div>
        </div>

        <div className="glass-panel p-6">
          <h3 className="text-lg font-semibold m-0 mb-6">待办审批分类</h3>
          <div className="flex-col gap-4">
            <div className="p-4 flex justify-between items-center" style={{ background: 'var(--panel-bg)', borderRadius: '8px', border: '1px solid var(--surface-border)' }}>
              <span className="font-semibold text-secondary">信息发布审核</span>
              <span className="text-lg font-bold text-danger">{stats?.details?.posts || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center" style={{ background: 'var(--panel-bg)', borderRadius: '8px', border: '1px solid var(--surface-border)' }}>
              <span className="font-semibold text-secondary">用户实名认证 (KYC)</span>
              <span className="text-lg font-bold text-danger">{stats?.details?.kyc || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center" style={{ background: 'var(--panel-bg)', borderRadius: '8px', border: '1px solid var(--surface-border)' }}>
              <span className="font-semibold text-secondary">头像/昵称审核</span>
              <span className="text-lg font-bold text-danger">{stats?.details?.profiles || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center" style={{ background: 'var(--panel-bg)', borderRadius: '8px', border: '1px solid var(--surface-border)' }}>
              <span className="font-semibold text-secondary">商家入驻申请</span>
              <span className="text-lg font-bold text-danger">{stats?.details?.merchants || 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
