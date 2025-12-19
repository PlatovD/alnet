<template>
  <section class="card">
    <h2>Welcome to Alnet</h2>
    <p v-if="!isAuthenticated">
      Please <RouterLink to="/login">log in</RouterLink> or
      <RouterLink to="/register">create an account</RouterLink> to continue.
    </p>
    <p v-else>
      You are logged in as <strong>{{ username }}</strong>.
      Go to your <RouterLink to="/profile">profile</RouterLink>.
    </p>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
  const { isAuthenticated, username } = storeToRefs(auth);
</script>

<style scoped>
.card h2 {
  font-size: 0.875rem;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0 0 1.5rem 0;
  opacity: 0.7;
}

.card p {
  font-size: 0.875rem;
  line-height: 1.8;
  opacity: 0.8;
}

.card strong {
  font-weight: 400;
  opacity: 1;
}
</style>

