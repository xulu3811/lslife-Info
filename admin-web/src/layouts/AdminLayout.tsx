import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, MessageSquareWarning, Settings, LogOut, UserCheck, Store, Layers, PackageOpen } from 'lucide-react';
import '../index.css';

const menuItems = [
  { path: '/dashboard', label: '数据大盘', icon: <LayoutDashboard size={20} /> },
  { path: '/users', label: '用户管理', icon: <Users size={20} /> },
  { path: '/kyc', label: '实名认证审核', icon: <UserCheck size={20} /> },
  { path: '/content', label: '内容审核', icon: <MessageSquareWarning size={20} /> },
  { path: '/categories', label: '类目管理', icon: <Layers size={20} /> },
  { path: '/merchants', label: '商家管理', icon: <Store size={20} /> },
  { path: '/app-version', label: 'App 版本管理', icon: <PackageOpen size={20} /> },
  { path: '/settings', label: '系统设置', icon: <Settings size={20} /> },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    navigate('/login');
  };

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <div 
        className="glass-panel flex-col" 
        style={{ 
          width: 260, 
          position: 'fixed', 
          top: 16, 
          bottom: 16, 
          left: 16, 
          display: 'flex', 
          zIndex: 10 
        }}
      >
        <div className="flex items-center gap-3 p-6" style={{ borderBottom: '1px solid var(--surface-border)' }}>
          <img src="/favicon.png" alt="LsLife Logo" style={{ width: '36px', height: '36px', objectFit: 'contain' }} />
          <div>
            <h2 className="text-lg font-semibold m-0">LsLife Admin</h2>
            <span className="text-xs text-success">● 安全防御运行中</span>
          </div>
        </div>

        <div className="flex-col flex-1 p-4 gap-2" style={{ overflowY: 'auto' }}>
          {menuItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className="flex items-center gap-3 py-2 px-4 w-full text-left"
                style={{
                  background: isActive ? 'var(--danger-bg)' : 'transparent',
                  color: isActive ? 'var(--primary)' : 'var(--text-secondary)',
                  border: 'none',
                  borderRadius: 'var(--radius-md)',
                  cursor: 'pointer',
                  fontWeight: isActive ? 600 : 500,
                  transition: 'all 0.2s ease',
                }}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
        </div>

        <div className="p-4" style={{ borderTop: '1px solid var(--surface-border)' }}>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 py-2 px-4 w-full text-danger font-medium text-left"
            style={{
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            <LogOut size={20} />
            安全登出
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-col flex-1" style={{ marginLeft: 292, padding: '16px 16px 16px 0', minHeight: '100vh' }}>
        <header className="glass-panel flex justify-between items-center px-6 py-4 mb-6">
          <h1 className="text-xl font-semibold m-0 text-gradient">
            {menuItems.find(i => location.pathname.startsWith(i.path))?.label || '管理控制台'}
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-secondary">最后登录IP: 115.191.6.95 (已白名单)</span>
            <div className="flex items-center justify-center font-bold" style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'linear-gradient(135deg, var(--primary), #FF7E67)', color: '#fff', boxShadow: '0 4px 12px rgba(229, 57, 53, 0.2)' }}>
              A
            </div>
          </div>
        </header>

        <main className="flex-col flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
