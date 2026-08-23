import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
});

// Request interceptor to inject short-lived JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token refresh and unauthorized errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        // For now, if unauthorized, redirect to login if not already there
        if (window.location.pathname !== '/admin-web/login') {
          window.location.href = '/admin-web/login';
        }
      } catch (err) {
        if (window.location.pathname !== '/admin-web/login') {
          window.location.href = '/admin-web/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
