import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import AdminLayout from './layouts/AdminLayout';
import Dashboard from './pages/Dashboard';
import ContentAudit from './pages/ContentAudit';
import KycAudit from './pages/KycAudit';
import UserManagement from './pages/UserManagement';
import { OrderManagement } from './pages/OrderManagement';
import { MerchantManagement } from './pages/MerchantManagement';
import { ProductAudit } from './pages/ProductAudit';
import CategoryManagement from './pages/CategoryManagement';
import { SystemSecurity } from './pages/SystemSecurity';
import AppVersionManagement from './pages/AppVersionManagement';

// Protected Route Guard
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export default function App() {
  return (
    <BrowserRouter basename="/admin-web">
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="content" element={<ContentAudit />} />
          <Route path="kyc" element={<KycAudit />} />
          <Route path="categories" element={<CategoryManagement />} />
          <Route path="orders" element={<OrderManagement />} />
          <Route path="merchants" element={<MerchantManagement />} />
          <Route path="products" element={<ProductAudit />} />
          <Route path="settings" element={<SystemSecurity />} />
          <Route path="app-version" element={<AppVersionManagement />} />
        </Route>

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
