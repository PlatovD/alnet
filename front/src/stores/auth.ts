import { defineStore } from 'pinia';
import api from '../api/client';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  userId: number | null;
  username: string | null;
  email: string | null;
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const [, payload] = token.split('.');
    if (!payload) return null;
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded) as Record<string, unknown>;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: null,
    refreshToken: null,
    userId: null,
    username: null,
    email: null
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken
  },
  actions: {
    loadFromStorage() {
      const raw = localStorage.getItem('alnet_auth');
      if (!raw) return;
      try {
        const parsed = JSON.parse(raw) as AuthState;
        this.$patch(parsed);
      } catch {
        // ignore
      }
    },
    persist() {
      const payload: AuthState = {
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        userId: this.userId,
        username: this.username,
        email: this.email
      };
      localStorage.setItem('alnet_auth', JSON.stringify(payload));
    },
    setTokens(token: string, refresh: string) {
      this.accessToken = token;
      this.refreshToken = refresh;
      const payload = decodeJwtPayload(token);
      if (payload) {
        const id = (payload as any).jti ?? (payload as any).id;
        const sub = (payload as any).sub;
        if (typeof id === 'string' || typeof id === 'number') {
          this.userId = Number(id);
        }
        if (typeof sub === 'string') {
          this.username = sub;
        }
      }
      this.persist();
    },
    setUserInfo(payload: { userId: number; username: string; email: string }) {
      this.userId = payload.userId;
      this.username = payload.username;
      this.email = payload.email;
      this.persist();
    },
    logout() {
      this.accessToken = null;
      this.refreshToken = null;
      this.userId = null;
      this.username = null;
      this.email = null;
      localStorage.removeItem('alnet_auth');
    },
    async signIn(username: string, password: string) {
      try {
        const { data } = await api.post('/auth/sign-in', { username, password });
        this.setTokens(data.token, data.refresh);
      } catch (e: any) {
        throw e;
      }
    },
    async signUp(username: string, email: string, password: string) {
      try {
        const { data } = await api.post('/auth/sign-up', { username, email, password });
        this.setTokens(data.token, data.refresh);
      } catch (e: any) {
        throw e;
      }
    },
    async refresh() {
      if (!this.refreshToken) return;
      const { data } = await api.post('/auth/refresh', { refresh: this.refreshToken });
      this.setTokens(data.token, data.refresh);
    },
    async fetchCurrentUser() {
      if (!this.userId) return;
      const { data } = await api.get(`/api/users/${this.userId}`);
      this.setUserInfo({
        userId: data.userId,
        username: data.username,
        email: data.email
      });
    }
  }
});


