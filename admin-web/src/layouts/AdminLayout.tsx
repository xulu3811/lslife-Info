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
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    navigate('/login');
  };

  const currentTitle = menuItems.find(i => location.pathname.startsWith(i.path))?.label || '系统设置';

  return (
    <div className="layout-container">
      {/* Material Left Sidebar */}
      <aside className="md-sidebar">
        <div className="md-sidebar-header">
          <img src="/admin-web/logo.png" alt="qylife Logo" style={{ width: '48px', height: '48px', objectFit: 'contain' }} />
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}>qylife</h2>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Admin Console</span>
          </div>
        </div>

        <nav className="md-nav">
          {menuItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`md-nav-item ${isActive ? 'active' : ''}`}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
          
          <div style={{ margin: '12px 0', borderTop: '1px solid var(--border-color)' }}></div>
          
          <button
            onClick={() => navigate('/settings')}
            className={`md-nav-item ${location.pathname.startsWith('/settings') ? 'active' : ''}`}
          >
            <Settings size={20} />
            系统设置
          </button>
        </nav>
      </aside>

      {/* Main Content Area */}
      <div className="md-main">
        {/* Top App Bar */}
        <header className="md-topbar">
          <h1 style={{ fontSize: '20px', fontWeight: 500, margin: 0 }}>{currentTitle}</h1>
          
          <div className="flex items-center gap-4">
            <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
              Server IP: 115.191.6.95
            </span>
            <div 
              style={{ 
                width: '36px', 
                height: '36px', 
                borderRadius: '50%', 
                background: 'var(--g-blue)', 
                color: '#fff', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
              onClick={handleLogout}
              title="登出"
            >
              A
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="md-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
