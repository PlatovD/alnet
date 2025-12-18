<template>
  <div class="app-shell">
    <header class="app-header">
      <h1 class="logo">Alnet</h1>
      <nav class="nav">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink v-if="!isAuthenticated" to="/login">Login</RouterLink>
        <RouterLink v-if="!isAuthenticated" to="/register">Register</RouterLink>
        <RouterLink v-if="isAuthenticated" to="/profile">Profile</RouterLink>
        <button v-if="isAuthenticated" class="link-button" @click="logout">
          Logout
        </button>
      </nav>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from './stores/auth';

const authStore = useAuthStore();
const { isAuthenticated } = storeToRefs(authStore);
const router = useRouter();

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>


