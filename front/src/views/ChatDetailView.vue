<template>
  <div class="chat-detail-layout">
    <div class="chat-header">
      <RouterLink to="/chats" class="back-link">← Back to chats</RouterLink>
      <h2>{{ chatName || `Chat #${chatId}` }}</h2>
      <button class="settings-button" @click="showSettings = !showSettings">
        Settings
      </button>
    </div>

    <!-- Settings Modal -->
    <div v-if="showSettings" class="settings-modal" @click.self="showSettings = false">
      <div class="settings-content card">
        <div class="settings-header">
          <h3>Chat Settings</h3>
          <button class="close-button" @click="showSettings = false">×</button>
        </div>

        <!-- Members Section -->
        <section class="settings-section">
          <h4>Members</h4>
          <div v-if="loadingMembers" class="loading-small">Loading members...</div>
          <div v-else-if="membersError" class="error">{{ membersError }}</div>
          <ul v-else class="members-list">
            <li v-for="member in members" :key="member.username" class="member-item">
              <span class="member-username">{{ member.username }}</span>
              <button
                  v-if="member.username !== currentUsername"
                  @click="onDeleteMember(member.username)"
                  class="delete-member-button"
                  :disabled="deletingMember"
              >
                Remove
              </button>
            </li>
          </ul>
          <button @click="loadMembers" class="refresh-button">Refresh</button>
          <p v-if="deleteMemberError" class="error">{{ deleteMemberError }}</p>
          <p v-if="deleteMemberSuccess" class="success">{{ deleteMemberSuccess }}</p>
        </section>

        <!-- Update Chat Name -->
        <section class="settings-section">
          <h4>Update Chat Name</h4>
          <form @submit.prevent="onUpdateChat" class="update-form">
            <input
                v-model="updateChatName"
                :placeholder="chatName || 'Chat name'"
                maxlength="30"
                required
            />
            <button type="submit" :disabled="updatingChat">
              {{ updatingChat ? 'Updating...' : 'Update' }}
            </button>
            <p v-if="updateError" class="error">{{ updateError }}</p>
            <p v-if="updateSuccess" class="success">{{ updateSuccess }}</p>
          </form>
        </section>

        <!-- Delete Chat -->
        <section class="settings-section danger-section">
          <h4>Danger Zone</h4>
          <p class="danger-text">Deleting a chat cannot be undone. All messages will be lost.</p>
          <button
              class="delete-button"
              @click="confirmDelete = true"
              :disabled="deletingChat"
          >
            Delete Chat
          </button>
          <div v-if="confirmDelete" class="confirm-delete">
            <p>Are you sure you want to delete this chat?</p>
            <div class="confirm-buttons">
              <button @click="onDeleteChat" :disabled="deletingChat" class="confirm-yes">
                Yes, Delete
              </button>
              <button @click="confirmDelete = false" :disabled="deletingChat" class="confirm-no">
                Cancel
              </button>
            </div>
            <p v-if="deleteError" class="error">{{ deleteError }}</p>
          </div>
        </section>
      </div>
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
import {onMounted, onUnmounted, ref, watch} from 'vue';
import {useRoute, useRouter, RouterLink} from 'vue-router';
import {storeToRefs} from 'pinia';
import api from '../api/client';
import {useAuthStore} from '../stores/auth';
import {MessageResponse} from "../service/websocket";
import websocket from "../service/websocket";
import {Message} from '@stomp/stompjs';


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
const {username: currentUsername} = storeToRefs(authStore);
const router = useRouter();

const chatId = ref<number>(Number(route.params.chatId));
const chatName = ref<string>('');

const messages = ref<MessageResponse[]>([]);
const loadingMessages = ref(false);
const messagesError = ref('');

const messageContent = ref('');
const sendingMessage = ref(false);
const sendError = ref('');

// Settings
const showSettings = ref(false);
const members = ref<{ username: string }[]>([]);
const loadingMembers = ref(false);
const membersError = ref('');
const updateChatName = ref('');
const updatingChat = ref(false);
const updateError = ref('');
const updateSuccess = ref('');
const confirmDelete = ref(false);
const deletingChat = ref(false);
const deleteError = ref('');
const deletingMember = ref(false);
const deleteMemberError = ref('');
const deleteMemberSuccess = ref('');

const connectWebSocket = () => {
  if (!authStore.accessToken) return;
  websocket.connect(authStore.accessToken, onConnectWebsocket, onWebsocketError);
}

const onWebsocketError = () => {
  console.error('Ошибка WebSocket!');
};

const onConnectWebsocket = () => {
  websocket.subscribe(`/topic/chats/${chatId.value}`, onReceiveMessage)
}

const onSendMessage = () => {
  if (!messageContent.value.trim()) return;
  websocket.sendMessage(`/app/chats/${chatId.value}`, messageContent.value);
  messageContent.value = '';
}

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

    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
  } catch {
    return dateTimeStr;
  }
};

const loadMessages = async () => {
  loadingMessages.value = true;
  messagesError.value = '';
  try {
    const {data} = await api.get<SliceResponse>(`/api/message/${chatId.value}`, {
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

const onReceiveMessage = (message: Message) => {
  const data: MessageResponse = JSON.parse(message.body);
  messages.value.push(data);
  setTimeout(() => {
    const container = document.querySelector('.messages-list');
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }, 100);
};


// Load chat name from chats list
const loadChatName = async () => {
  try {
    const {data} = await api.get('/api/membership');
    const chat = (data.chats || []).find((c: any) => c.chatId === chatId.value);
    if (chat) {
      chatName.value = chat.name || '';
      updateChatName.value = chat.name || '';
    }
  } catch (e) {
    console.error('Failed to load chat name', e);
  }
};

// Load members
const loadMembers = async () => {
  loadingMembers.value = true;
  membersError.value = '';
  try {
    const {data} = await api.get(`/api/membership/${chatId.value}`);
    members.value = data.members || [];
  } catch (e: any) {
    console.error(e);
    membersError.value = e?.response?.status === 403
        ? 'You are not a member of this chat.'
        : 'Failed to load members.';
  } finally {
    loadingMembers.value = false;
  }
};

// Update chat
const onUpdateChat = async () => {
  if (!updateChatName.value.trim()) return;

  updatingChat.value = true;
  updateError.value = '';
  updateSuccess.value = '';
  try {
    const {data} = await api.put(`/api/chats/${chatId.value}`, {
      chatId: null,
      name: updateChatName.value.trim(),
      members: []
    });
    chatName.value = data.name;
    updateSuccess.value = 'Chat name updated successfully.';
    setTimeout(() => {
      updateSuccess.value = '';
    }, 3000);
  } catch (e: any) {
    console.error(e);
    updateError.value = e?.response?.status === 403
        ? 'You are not a member of this chat.'
        : 'Failed to update chat name.';
  } finally {
    updatingChat.value = false;
  }
};

// Delete chat
const onDeleteChat = async () => {
  deletingChat.value = true;
  deleteError.value = '';
  try {
    await api.delete(`/api/chats/${chatId.value}`);
    router.push('/chats');
  } catch (e: any) {
    console.error(e);
    deleteError.value = e?.response?.status === 403
        ? 'You are not a member of this chat.'
        : 'Failed to delete chat.';
    deletingChat.value = false;
  }
};

// Delete member
const onDeleteMember = async (username: string) => {
  if (!confirm(`Remove ${username} from this chat?`)) return;

  deletingMember.value = true;
  deleteMemberError.value = '';
  deleteMemberSuccess.value = '';
  try {
    await api.delete(`/api/membership/${chatId.value}`, {
      data: {
        chatId: chatId.value,
        usernames: [username]
      }
    });
    deleteMemberSuccess.value = `${username} removed from chat.`;
    setTimeout(() => {
      deleteMemberSuccess.value = '';
    }, 3000);
    await loadMembers();
  } catch (e: any) {
    console.error(e);
    deleteMemberError.value = e?.response?.status === 403
        ? 'You are not a member of this chat.'
        : 'Failed to remove member.';
  } finally {
    deletingMember.value = false;
  }
};

// Watch for settings modal opening to load members
watch(showSettings, (isOpen) => {
  if (isOpen) {
    loadMembers();
  }
});

watch(() => route.params.chatId, (newId) => {
  chatId.value = Number(newId);
  loadMessages();
  loadChatName();
  if (showSettings.value) {
    loadMembers();
  }
});

onMounted(() => {
  loadMessages();
  loadChatName();
  connectWebSocket();
});

onUnmounted(() => {
  websocket.disconnect();
});
</script>

<style scoped>
.chat-detail-layout {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  width: 100%;
  max-width: 800px;
  height: calc(100vh - 120px);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0;
  justify-content: space-between;
}

.back-link {
  color: #ffffff;
  text-decoration: none;
  font-size: 0.875rem;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.back-link:hover {
  opacity: 1;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 400;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.settings-button {
  padding: 0.5rem 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 400;
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: all 0.2s;
}

.settings-button:hover {
  background: #ffffff;
  color: #000000;
}

.messages-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-width: none;
  padding: 0;
  border: 1px solid #1a1a1a;
}

.loading,
.empty {
  text-align: center;
  color: #ffffff;
  opacity: 0.5;
  padding: 3rem 2rem;
  font-size: 0.875rem;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem;
}

.messages-list::-webkit-scrollbar {
  width: 2px;
}

.messages-list::-webkit-scrollbar-track {
  background: #000000;
}

.messages-list::-webkit-scrollbar-thumb {
  background: #1a1a1a;
}

.message-item {
  padding: 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  max-width: 70%;
  align-self: flex-start;
}

.message-item.own-message {
  align-self: flex-end;
  border-color: #ffffff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.75rem;
}

.message-username {
  font-weight: 400;
  color: #ffffff;
  opacity: 0.9;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.message-time {
  color: #ffffff;
  opacity: 0.5;
  font-size: 0.7rem;
}

.message-content {
  color: #ffffff;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 0.875rem;
  line-height: 1.6;
}

.message-form-container {
  max-width: none;
  padding: 0;
  border: 1px solid #1a1a1a;
}

.message-form-container form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem;
}

.message-form-container textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
  min-height: 80px;
}

.message-form-container textarea:focus {
  outline: none;
  border-color: #ffffff;
}

.message-form-container button {
  align-self: flex-end;
}

/* Settings Modal */
.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.settings-content {
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  border: 1px solid #1a1a1a;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #1a1a1a;
}

.settings-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.close-button {
  background: transparent;
  border: 1px solid #1a1a1a;
  color: #ffffff;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-button:hover {
  background: #ffffff;
  color: #000000;
}

.settings-section {
  margin-bottom: 2.5rem;
}

.settings-section h4 {
  margin: 0 0 1rem 0;
  font-size: 0.75rem;
  font-weight: 400;
  color: #ffffff;
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.members-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.member-item {
  padding: 0.75rem 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-username {
  color: #ffffff;
  font-weight: 400;
  font-size: 0.875rem;
}

.delete-member-button {
  padding: 0.4rem 0.8rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.2s;
}

.delete-member-button:hover:not(:disabled) {
  background: #ffffff;
  color: #000000;
}

.delete-member-button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.refresh-button {
  padding: 0.5rem 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-button:hover {
  background: #ffffff;
  color: #000000;
}

.update-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.update-form input {
  padding: 0.75rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-family: inherit;
  font-size: 0.875rem;
}

.update-form input:focus {
  outline: none;
  border-color: #ffffff;
}

.update-form button {
  align-self: flex-start;
}

.danger-section {
  border-top: 1px solid #1a1a1a;
  padding-top: 2rem;
}

.danger-text {
  color: #ffffff;
  opacity: 0.5;
  font-size: 0.75rem;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.delete-button {
  padding: 0.5rem 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.delete-button:hover:not(:disabled) {
  background: #ffffff;
  color: #000000;
}

.delete-button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.confirm-delete {
  margin-top: 1rem;
  padding: 1.5rem;
  border: 1px solid #1a1a1a;
  background: #000000;
}

.confirm-delete p {
  margin: 0 0 1rem 0;
  color: #ffffff;
  opacity: 0.7;
  font-size: 0.875rem;
}

.confirm-buttons {
  display: flex;
  gap: 0.75rem;
}

.confirm-yes,
.confirm-no {
  padding: 0.5rem 1rem;
  border: 1px solid #1a1a1a;
  background: #000000;
  color: #ffffff;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.confirm-yes:hover:not(:disabled),
.confirm-no:hover:not(:disabled) {
  background: #ffffff;
  color: #000000;
}

.loading-small {
  color: #ffffff;
  opacity: 0.5;
  font-size: 0.875rem;
  padding: 0.5rem 0;
}
</style>


