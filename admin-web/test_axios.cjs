const axios = require('axios');
const api = axios.create({ baseURL: '/api' });
console.log(api.getUri({ url: '/admin/login' }));
