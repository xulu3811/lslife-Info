import { useEffect, useState } from 'react';
import { Users, ShoppingBag, Activity, AlertTriangle } from 'lucide-react';
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
    activeOrders: 0,
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
        <StatCard title="今日新增用户" value={stats.newUsers.toString()} trend={12.5} icon={Users} color="229, 57, 53" linkTo="/users" />
        <StatCard title="活跃订单数" value={stats.activeOrders.toString()} trend={8.2} icon={ShoppingBag} color="167, 139, 250" linkTo="/orders" />
        <StatCard title="平台流水 (元)" value={`￥${stats.revenue}`} trend={-2.4} icon={Activity} color="16, 185, 129" />
        <StatCard title="待审核内容" value={stats.pendingReviews.toString()} trend={100} icon={AlertTriangle} color="239, 68, 68" linkTo="/content" />
      </div>

      <div className="grid gap-6" style={{ gridTemplateColumns: '2fr 1fr' }}>
        <div className="glass-panel p-6" style={{ minHeight: '400px' }}>
          <h3 className="text-lg font-semibold m-0 mb-6">平台核心指标趋势 (安全快照)</h3>
          <div className="h-full" style={{ minHeight: '300px' }}>
            {stats.trendData && stats.trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={stats.trendData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                  <Line type="monotone" name="新增用户" dataKey="users" stroke="#e53935" strokeWidth={2} activeDot={{ r: 6 }} />
                  <Line type="monotone" name="营收流水(元)" dataKey="revenue" stroke="#10b981" strokeWidth={2} />
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
          <h3 className="text-lg font-semibold m-0 mb-6">最新安全告警</h3>
          <div className="flex-col gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="p-4" style={{ background: 'var(--danger-bg)', borderRadius: '8px', borderLeft: '4px solid var(--danger)' }}>
                <div className="flex justify-between mb-2">
                  <span className="font-semibold text-danger">异常异地登录拦截</span>
                  <span className="text-xs text-secondary">10分钟前</span>
                </div>
                <p className="m-0 text-sm text-secondary">尝试登录账号: admin_test，来源IP: 182.xx.xx.xx (已被策略阻断)</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
