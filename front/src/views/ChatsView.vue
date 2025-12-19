<template>
  <div class="chats-layout">
    <section class="card chats-card">
      <h2>Your chats</h2>
      <p v-if="loadingChats">Loading chats...</p>
      <p v-else-if="chats.length === 0">You have no chats yet.</p>
      <ul v-else class="chat-list">
        <li v-for="chat in chats" :key="chat.chatId" class="chat-item" @click="openChat(chat.chatId)">
          <div class="chat-name">{{ chat.name || '(no name)' }}</div>
          <div class="chat-meta">
            <span>ID: {{ chat.chatId }}</span>
            <span v-if="chat.members && chat.members.length">Members: {{ chat.members.join(', ') }}</span>
          </div>
        </li>
      </ul>
      <p v-if="chatsError" class="error">{{ chatsError }}</p>
    </section>

    <section class="card form-card create-chat-card">
      <h2>Create new chat</h2>
      <form @submit.prevent="onCreateChat">
        <label>
          Chat name
          <input v-model="chatName" maxlength="30" />
        </label>
        <label>
          Members (comma separated usernames)
          <input
            v-model="membersInput"
            placeholder="user1, user2, user3"
          />
        </label>
        <button type="submit" :disabled="creatingChat">
          {{ creatingChat ? 'Creating...' : 'Create chat' }}
        </button>
        <p v-if="createError" class="error">{{ createError }}</p>
        <p v-if="createSuccess" class="success">{{ createSuccess }}</p>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import api from '../api/client';
import { useAuthStore } from '../stores/auth';

interface ChatDTO {
  chatId: number;
  name: string;
  members?: string[];
}

interface UserChatsMembershipResponse {
  username: string;
  chats: ChatDTO[];
}

const authStore = useAuthStore();
const { username: currentUsername } = storeToRefs(authStore);
const router = useRouter();

const chats = ref<ChatDTO[]>([]);
const loadingChats = ref(false);
const chatsError = ref('');

const chatName = ref('');
const membersInput = ref('');
const creatingChat = ref(false);
const createError = ref('');
const createSuccess = ref('');

const loadChats = async () => {
  loadingChats.value = true;
  chatsError.value = '';
  try {
    const { data } = await api.get<UserChatsMembershipResponse>('/api/membership');
    chats.value = data.chats || [];
  } catch (e) {
    console.error(e);
    chatsError.value = 'Failed to load chats.';
  } finally {
    loadingChats.value = false;
  }
};

const onCreateChat = async () => {
  if (!currentUsername.value) {
    createError.value = 'You must be logged in to create a chat.';
    return;
  }

  creatingChat.value = true;
  createError.value = '';
  createSuccess.value = '';
  try {
    const members = membersInput.value
      .split(',')
      .map((m) => m.trim())
      .filter((m) => m.length > 0);

    // Always include the current user (creator) in the members list
    if (!members.includes(currentUsername.value)) {
      members.push(currentUsername.value);
    }

    await api.post('/api/chats', {
      chatId: null,
      name: chatName.value,
      members
    });

    createSuccess.value = 'Chat created.';
    chatName.value = '';
    membersInput.value = '';
    await loadChats();
  } catch (e) {
    console.error(e);
    createError.value = 'Failed to create chat.';
  } finally {
    creatingChat.value = false;
  }
};

const openChat = (chatId: number) => {
  router.push({ name: 'chat-detail', params: { chatId: String(chatId) } });
};

onMounted(() => {
  loadChats();
});
</script>

<style scoped>
.chats-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.5fr);
  gap: 2rem;
  width: 100%;
  max-width: 1000px;
}

@media (max-width: 900px) {
  .chats-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

.chats-card {
  max-width: none;
  border: 1px solid #1a1a1a;
  padding: 2rem;
}

.create-chat-card {
  max-width: none;
  border: 1px solid #1a1a1a;
  padding: 2rem;
}

.chats-card h2,
.create-chat-card h2 {
  font-size: 0.875rem;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0 0 1.5rem 0;
  opacity: 0.7;
}

.chat-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.chat-item {
  padding: 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  border-color: #ffffff;
}

.chat-name {
  font-weight: 400;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.chat-meta {
  font-size: 0.7rem;
  color: #ffffff;
  opacity: 0.5;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>



