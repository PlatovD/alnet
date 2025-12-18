<template>
  <section class="card form-card">
    <h2>Register</h2>
    <form @submit.prevent="onSubmit">
      <label>
        Username
        <input v-model="username" required autocomplete="username" />
      </label>
      <label>
        Email
        <input v-model="email" type="email" required autocomplete="email" />
      </label>
      <label>
        Password
        <input v-model="password" type="password" required autocomplete="new-password" />
      </label>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating account...' : 'Register' }}
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
const email = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const onSubmit = async () => {
  loading.value = true;
  error.value = '';
  try {
    await auth.signUp(username.value, email.value, password.value);
    router.push('/profile');
  } catch (e) {
    error.value = 'Registration failed. Try different username/email.';
    console.error(e);
  } finally {
    loading.value = false;
  }
};
</script>


