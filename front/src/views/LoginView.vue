<template>
  <section class="card form-card">
    <h2>Login</h2>
    <form @submit.prevent="onSubmit">
      <label>
        Username
        <input v-model="username" required autocomplete="username" />
      </label>
      <label>
        Password
        <input v-model="password" type="password" required autocomplete="current-password" />
      </label>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Logging in...' : 'Login' }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const onSubmit = async () => {
  loading.value = true;
  error.value = '';
  try {
    await auth.signIn(username.value, password.value);
    router.push('/profile');
  } catch (e) {
    error.value = 'Login failed. Check your credentials.';
    console.error(e);
  } finally {
    loading.value = false;
  }
  };
</script>

<style scoped>
.card h2 {
  font-size: 0.875rem;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0 0 2rem 0;
  opacity: 0.7;
}
</style>

