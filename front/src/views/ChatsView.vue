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
  gap: 1.5rem;
  width: 100%;
  max-width: 960px;
}

@media (max-width: 900px) {
  .chats-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

.chats-card {
  max-width: none;
}

.create-chat-card {
  max-width: none;
}

.chat-list {
  list-style: none;
  padding: 0;
  margin: 0.75rem 0 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.chat-item {
  padding: 0.6rem 0.75rem;
  border-radius: 0.6rem;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.25);
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  background: rgba(30, 41, 59, 0.95);
  border-color: rgba(148, 163, 184, 0.4);
  transform: translateX(2px);
}

.chat-name {
  font-weight: 600;
  margin-bottom: 0.15rem;
}

.chat-meta {
  font-size: 0.8rem;
  color: #9ca3af;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>



