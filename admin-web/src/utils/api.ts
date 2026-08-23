import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// Add a request interceptor to inject the token
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

// Add a response interceptor to handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('admin_token');
      // Redirect to login if not already there
      if (window.location.pathname !== '/admin-web/login') {
        window.location.href = '/admin-web/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
