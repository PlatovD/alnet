import { defineStore } from 'pinia';
import api from '../api/client';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  userId: number | null;
  username: string | null;
  email: string | null;
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
    setTokens(access: string, refresh: string) {
      this.accessToken = access;
      this.refreshToken = refresh;
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
      console.log('[auth] signIn request payload', { username, password });
      try {
        const { data } = await api.post('/auth/sign-in', { username, password });
        console.log('[auth] signIn response', data);
        // adjust field names if your DTO is different
        this.setTokens(data.access, data.refresh);
      } catch (e: any) {
        console.error('[auth] signIn error', {
          message: e?.message,
          status: e?.response?.status,
          data: e?.response?.data
        });
        throw e;
      }
    },
    async signUp(username: string, email: string, password: string) {
      console.log('[auth] signUp request payload', { username, email, password });
      try {
        const { data } = await api.post('/auth/sign-up', { username, email, password });
        console.log('[auth] signUp response', data);
        this.setTokens(data.access, data.refresh);
      } catch (e: any) {
        console.error('[auth] signUp error', {
          message: e?.message,
          status: e?.response?.status,
          data: e?.response?.data
        });
        throw e;
      }
    },
    async refresh() {
      if (!this.refreshToken) return;
      const { data } = await api.post('/auth/refresh', { refresh: this.refreshToken });
      this.setTokens(data.access, data.refresh);
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


