<template>
  <div class="chat-detail-layout">
    <div class="chat-header">
      <RouterLink to="/chats" class="back-link">← Back to chats</RouterLink>
      <h2>{{ chatName || `Chat #${chatId}` }}</h2>
    </div>

    <div class="messages-container card">
      <div v-if="loadingMessages" class="loading">Loading messages...</div>
      <div v-else-if="messagesError" class="error">{{ messagesError }}</div>
      <div v-else-if="messages.length === 0" class="empty">No messages yet. Start the conversation!</div>
      <div v-else class="messages-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'own-message': msg.username === currentUsername }"
        >
          <div class="message-header">
            <span class="message-username">{{ msg.username }}</span>
            <span class="message-time">{{ formatDateTime(msg.dateTime) }}</span>
          </div>
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>
    </div>

    <div class="message-form-container card">
      <form @submit.prevent="onSendMessage">
        <label>
          <textarea
            v-model="messageContent"
            placeholder="Type your message..."
            rows="3"
            maxlength="5000"
            required
          ></textarea>
        </label>
        <button type="submit" :disabled="sendingMessage">
          {{ sendingMessage ? 'Sending...' : 'Send' }}
        </button>
        <p v-if="sendError" class="error">{{ sendError }}</p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
import { storeToRefs } from 'pinia';
import api from '../api/client';
import { useAuthStore } from '../stores/auth';

interface MessageResponse {
  id: number;
  content: string;
  dateTime: string;
  username: string;
  chatId: number;
}

interface SliceResponse {
  content: MessageResponse[];
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
}

const route = useRoute();
const authStore = useAuthStore();
const { username: currentUsername } = storeToRefs(authStore);

const chatId = ref<number>(Number(route.params.chatId));
const chatName = ref<string>('');

const messages = ref<MessageResponse[]>([]);
const loadingMessages = ref(false);
const messagesError = ref('');

const messageContent = ref('');
const sendingMessage = ref(false);
const sendError = ref('');

const formatDateTime = (dateTimeStr: string): string => {
  if (!dateTimeStr) return '';
  try {
    const date = new Date(dateTimeStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return dateTimeStr;
  }
};

const loadMessages = async () => {
  loadingMessages.value = true;
  messagesError.value = '';
  try {
    const { data } = await api.get<SliceResponse>(`/api/message/${chatId.value}`, {
      params: {
        size: 100,
        sort: 'dateTime,desc'
      }
    });
    // Reverse to show oldest first (since backend returns DESC)
    messages.value = [...(data.content || [])].reverse();
  } catch (e: any) {
    console.error(e);
    messagesError.value = e?.response?.status === 403
      ? 'You are not a member of this chat.'
      : 'Failed to load messages.';
  } finally {
    loadingMessages.value = false;
  }
};

const onSendMessage = async () => {
  if (!messageContent.value.trim()) return;

  sendingMessage.value = true;
  sendError.value = '';
  try {
    const { data } = await api.post<MessageResponse>(`/api/message/${chatId.value}`, {
      content: messageContent.value.trim()
    });

    // Add the new message to the list
    messages.value.push(data);
    messageContent.value = '';
    
    // Scroll to bottom
    setTimeout(() => {
      const container = document.querySelector('.messages-list');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  } catch (e: any) {
    console.error(e);
    sendError.value = e?.response?.status === 403
      ? 'You are not a member of this chat.'
      : 'Failed to send message.';
  } finally {
    sendingMessage.value = false;
  }
};

// Load chat name from chats list
const loadChatName = async () => {
  try {
    const { data } = await api.get('/api/membership');
    const chat = (data.chats || []).find((c: any) => c.chatId === chatId.value);
    if (chat) {
      chatName.value = chat.name || '';
    }
  } catch (e) {
    console.error('Failed to load chat name', e);
  }
};

watch(() => route.params.chatId, (newId) => {
  chatId.value = Number(newId);
  loadMessages();
  loadChatName();
});

onMounted(() => {
  loadMessages();
  loadChatName();
});
</script>

<style scoped>
.chat-detail-layout {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 800px;
  height: calc(100vh - 120px);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 0;
}

.back-link {
  color: #38bdf8;
  text-decoration: none;
  font-size: 0.9rem;
}

.back-link:hover {
  text-decoration: underline;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.messages-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-width: none;
  padding: 1rem;
}

.loading,
.empty {
  text-align: center;
  color: #9ca3af;
  padding: 2rem;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-right: 0.5rem;
}

.messages-list::-webkit-scrollbar {
  width: 6px;
}

.messages-list::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.5);
  border-radius: 3px;
}

.messages-list::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

.messages-list::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.message-item {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  max-width: 75%;
  align-self: flex-start;
}

.message-item.own-message {
  align-self: flex-end;
  background: rgba(249, 115, 22, 0.15);
  border-color: rgba(249, 115, 22, 0.3);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
  font-size: 0.85rem;
}

.message-username {
  font-weight: 600;
  color: #38bdf8;
}

.own-message .message-username {
  color: #fb923c;
}

.message-time {
  color: #9ca3af;
  font-size: 0.75rem;
}

.message-content {
  color: #e5e7eb;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-form-container {
  max-width: none;
  padding: 1rem;
}

.message-form-container form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.message-form-container textarea {
  width: 100%;
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.8);
  color: #e5e7eb;
  font-family: inherit;
  resize: vertical;
  min-height: 60px;
}

.message-form-container textarea:focus {
  outline: none;
  border-color: #38bdf8;
}

.message-form-container button {
  align-self: flex-end;
}
</style>

