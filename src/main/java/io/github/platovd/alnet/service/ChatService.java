package io.github.platovd.alnet.service;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.exception.chat.ChatNotFoundException;
import io.github.platovd.alnet.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public Chat createChat(String chatName) {
        Chat chat = Chat.builder().chatName(chatName).build();
        return chatRepository.save(chat);
    }

    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("No chat with this id"));
    }

    public Chat updateChat(ChatCreationOrUpdateRequest request) {
        Chat chat = getChatById(request.getChatId());
        chat.setChatName(request.getName());
        chatRepository.save(chat);
        return chat;
    }

    public void deleteChat(Chat chat) {
        chatRepository.deleteById(chat.getChatId());
    }
}
