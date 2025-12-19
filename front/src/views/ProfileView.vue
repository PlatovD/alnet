<template>
  <section class="card form-card" v-if="userId">
    <h2>Profile</h2>
    <form @submit.prevent="onSave">
      <label>
        ID
        <input :value="userId" disabled />
      </label>
      <label>
        Username
        <input v-model="username" required />
      </label>
      <label>
        Email
        <input v-model="email" type="email" required />
      </label>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Saving...' : 'Save' }}
      </button>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </section>
  <section v-else class="card">
    <p>No user loaded. Try re-login.</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/auth';
import api from '../api/client';

const auth = useAuthStore();
const { userId } = storeToRefs(auth);

const username = ref(auth.username || '');
const email = ref(auth.email || '');
const loading = ref(false);
const message = ref('');
const error = ref('');

watch(
  () => auth.username,
  (val) => {
    if (val) username.value = val;
  }
);
watch(
  () => auth.email,
  (val) => {
    if (val) email.value = val;
  }
);

onMounted(async () => {
  auth.loadFromStorage();
  if (!auth.userId && auth.isAuthenticated) {
    // optional: you could have an endpoint to get "me"
    // here we assume userId already known after login/registration
  }
  if (auth.userId) {
    try {
      await auth.fetchCurrentUser();
    } catch (e) {
      console.error(e);
    }
  }
});

const onSave = async () => {
  if (!userId.value) return;
  loading.value = true;
  message.value = '';
  error.value = '';
  try {
    const { data } = await api.put(`/api/users/${userId.value}`, {
      userId: userId.value,
      username: username.value,
      email: email.value
    });
    auth.setUserInfo({
      userId: data.userId,
      username: data.username,
      email: data.email
    });
    message.value = 'Profile updated.';
  } catch (e) {
    error.value = 'Failed to update profile.';
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

input:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
</style>

