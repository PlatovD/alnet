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


