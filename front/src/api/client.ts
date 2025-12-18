import axios from 'axios';
import { useAuthStore } from '../stores/auth';

const apiBase =
  import.meta.env.VITE_API_BASE ||
  (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8080');

export const api = axios.create({
  baseURL: apiBase
});

api.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  const token = authStore.accessToken;
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;


