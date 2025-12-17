package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.exception.base.WrongDataException;
import io.github.platovd.alnet.exception.chat.ChatNotFoundException;
import io.github.platovd.alnet.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository repository;

    @Transactional
    public Chat getChatById(Long chatId) {
        return repository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("No chat with this id"));
    }

    @Transactional
    public Chat createChat(String name) {
        Chat chat = Chat.builder().chatName(name).build();
        return repository.save(chat);
    }

    @Transactional
    public Chat updateChat(Long chatId, ChatCreationOrUpdateRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("DTO and url data isn't the same");
        Chat chat = getChatById(request.getChatId());
        chat.setChatName(request.getName());
        repository.save(chat);
        return chat;
    }

    @Transactional
    public void deleteChatById(Long chatId) {
        repository.deleteById(chatId);
    }
}
