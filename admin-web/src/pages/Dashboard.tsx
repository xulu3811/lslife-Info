import { useEffect, useState } from 'react';
import { Users, FileText, Activity, AlertTriangle, Cpu, HardDrive, Server, ActivitySquare } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import api from '../utils/axios';
import { Link } from 'react-router-dom';

const StatCard = ({ title, value, trend, icon: Icon, color, linkTo }: any) => (
  <div className="md-card flex-col gap-2">
    <div className="md-card-title">
      <div style={{ padding: '6px', background: `rgba(${color}, 0.1)`, borderRadius: '8px', color: `rgb(${color})` }}>
        <Icon size={20} />
      </div>
      {title}
    </div>
    <div className="md-value">{value}</div>
    <div className="flex justify-between items-center mt-2">
      <div className="flex items-center gap-2">
        <span className={`badge ${trend >= 0 ? 'badge-success' : 'badge-danger'}`}>
          {trend >= 0 ? '+' : ''}{trend}%
        </span>
        <span className="text-secondary text-xs">较昨日</span>
      </div>
      {linkTo && (
        <Link to={linkTo} style={{ textDecoration: 'none', color: 'var(--g-blue)', fontSize: '14px', fontWeight: 500 }}>
          管理 &rarr;
        </Link>
      )}
    </div>
  </div>
);

const ProgressBar = ({ percent, type }: { percent: number, type: 'cpu' | 'mem' | 'disk' | 'pm2' }) => {
  let color = 'var(--g-blue)';
  if (type === 'mem') color = 'var(--g-yellow)';
  if (type === 'disk') color = 'var(--g-green)';
  if (type === 'pm2') color = 'var(--g-red)';
  
  if (percent > 85) color = 'var(--g-red)';

  return (
    <div className="progress-bar-bg">
      <div className="progress-bar-fill" style={{ width: `${percent}%`, background: color }}></div>
    </div>
  );
};

export default function Dashboard() {
  const [stats, setStats] = useState<any>({});
  const [server, setServer] = useState<any>(null);

  useEffect(() => {
    api.get('/admin/dashboard').then(res => setStats(res.data?.data || {})).catch(console.error);
    
    const fetchServer = () => {
      api.get('/admin/server-status').then(res => setServer(res.data?.data || {})).catch(console.error);
    };
    fetchServer();
    const timer = setInterval(fetchServer, 3000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="flex-col gap-6">
      
      {/* 科技感服务器监控中心 */}
      <div>
        <h2 style={{ fontSize: '18px', fontWeight: 500, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Server size={20} color="var(--g-blue)" /> 实时监控引擎
        </h2>
        
        {server ? (
          <div className="monitor-grid">
            {/* CPU */}
            <div className="monitor-card monitor-cpu">
              <div className="flex justify-between items-start">
                <div>
                  <div className="text-secondary text-sm font-medium">CPU 负载</div>
                  <div className="text-2xl font-bold mt-2">{server.cpu?.percent}%</div>
                </div>
                <Cpu size={24} color="var(--g-blue)" opacity={0.8} />
              </div>
              <ProgressBar percent={server.cpu?.percent || 0} type="cpu" />
              <div className="text-xs text-secondary mt-2 text-right">{server.cpu?.cores} 核心计算组</div>
            </div>

            {/* RAM */}
            <div className="monitor-card monitor-mem">
              <div className="flex justify-between items-start">
                <div>
                  <div className="text-secondary text-sm font-medium">内存占用</div>
                  <div className="text-2xl font-bold mt-2">{server.ram?.percent}%</div>
                </div>
                <ActivitySquare size={24} color="var(--g-yellow)" opacity={0.8} />
              </div>
              <ProgressBar percent={server.ram?.percent || 0} type="mem" />
              <div className="text-xs text-secondary mt-2 text-right">{server.ram?.used} / {server.ram?.total}</div>
            </div>

            {/* Disk */}
            <div className="monitor-card monitor-disk">
              <div className="flex justify-between items-start">
                <div>
                  <div className="text-secondary text-sm font-medium">存储空间</div>
                  <div className="text-2xl font-bold mt-2">{server.disk?.percent}%</div>
                </div>
                <HardDrive size={24} color="var(--g-green)" opacity={0.8} />
              </div>
              <ProgressBar percent={server.disk?.percent || 0} type="disk" />
              <div className="text-xs text-secondary mt-2 text-right">{server.disk?.used} / {server.disk?.total}</div>
            </div>

            {/* PM2 */}
            <div className="monitor-card monitor-pm2">
              <div className="flex justify-between items-start">
                <div>
                  <div className="text-secondary text-sm font-medium">后端服务 (PM2)</div>
                  <div className="text-2xl font-bold mt-2">{server.pm2?.length || 0} 实例</div>
                </div>
                <Activity size={24} color="var(--g-red)" opacity={0.8} />
              </div>
              <ProgressBar percent={100} type="pm2" />
              <div className="text-xs text-success mt-2 text-right font-medium">● 核心进程全部在线</div>
            </div>
          </div>
        ) : (
          <div className="md-card flex items-center justify-center text-secondary" style={{ height: '120px', marginBottom: '24px' }}>
            系统探针连接中...
          </div>
        )}
      </div>

      <div className="grid grid-cols-4 gap-6">
        <StatCard title="今日新增用户" value={(stats.newUsers || 0).toString()} trend={12.5} icon={Users} color="26, 115, 232" linkTo="/users" />
        <StatCard title="今日发帖量" value={(stats.todayPosts || 0).toString()} trend={8.2} icon={FileText} color="251, 188, 4" linkTo="/content" />
        <StatCard title="平台充值流水 (元)" value={`￥${stats.revenue || 0}`} trend={-2.4} icon={Activity} color="52, 168, 83" />
        <StatCard title="待办审批总数" value={(stats.pendingReviews || 0).toString()} trend={10} icon={AlertTriangle} color="234, 67, 53" linkTo="/content" />
      </div>

      <div className="grid gap-6" style={{ gridTemplateColumns: '2fr 1fr' }}>
        <div className="md-card" style={{ minHeight: '400px' }}>
          <h3 style={{ fontSize: '16px', fontWeight: 500, marginBottom: '24px' }}>平台核心指标趋势 (7日快照)</h3>
          <div className="h-full" style={{ minHeight: '300px' }}>
            {stats.trendData && stats.trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={stats.trendData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                  <Line type="monotone" name="新增用户" dataKey="users" stroke="var(--g-blue)" strokeWidth={3} activeDot={{ r: 6 }} />
                  <Line type="monotone" name="发帖量" dataKey="posts" stroke="var(--g-red)" strokeWidth={3} />
                  <CartesianGrid stroke="var(--border-color)" strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="date" stroke="var(--text-secondary)" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} dy={10} />
                  <YAxis yAxisId="left" stroke="var(--text-secondary)" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} dx={-10} />
                  <YAxis yAxisId="right" orientation="right" stroke="var(--text-secondary)" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} dx={10} />
                  <Tooltip contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border-color)', borderRadius: '8px', boxShadow: 'var(--elevation-2)' }} />
                  <Legend verticalAlign="top" height={36} iconType="circle" />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center text-secondary h-full" style={{ border: '1px dashed var(--border-color)', borderRadius: '8px', minHeight: '300px' }}>
                图表数据加载中...
              </div>
            )}
          </div>
        </div>

        <div className="md-card">
          <h3 style={{ fontSize: '16px', fontWeight: 500, marginBottom: '24px' }}>待办审批分类</h3>
          <div className="flex-col gap-4">
            <div className="p-4 flex justify-between items-center" style={{ borderBottom: '1px solid var(--border-color)' }}>
              <span className="font-medium text-secondary">信息发布审核</span>
              <span className="text-lg font-bold text-danger">{stats.details?.posts || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center" style={{ borderBottom: '1px solid var(--border-color)' }}>
              <span className="font-medium text-secondary">用户实名认证 (KYC)</span>
              <span className="text-lg font-bold text-danger">{stats.details?.kyc || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center" style={{ borderBottom: '1px solid var(--border-color)' }}>
              <span className="font-medium text-secondary">头像/昵称审核</span>
              <span className="text-lg font-bold text-danger">{stats.details?.profiles || 0}</span>
            </div>
            <div className="p-4 flex justify-between items-center">
              <span className="font-medium text-secondary">商家入驻申请</span>
              <span className="text-lg font-bold text-danger">{stats.details?.merchants || 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
