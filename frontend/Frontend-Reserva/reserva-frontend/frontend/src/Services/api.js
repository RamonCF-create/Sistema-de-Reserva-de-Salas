import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }

    if (error.response?.status === 403) {
      if (!localStorage.getItem('token')) {
        alert('Sessao expirada. Faca login novamente.');
        window.location.href = '/login';
      } else {
        alert('Sem permissao');
      }
    }

    if (error.response?.status === 422) {
      console.log(error.response.data);
    }

    return Promise.reject(error);
  }
);

export default api;
