package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.chat.response.ChatResponse;
import io.github.platovd.alnet.entity.Chat;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ChatMapper implements MapperFromEntityToDTO<Chat, ChatResponse> {
    @Override
    public ChatResponse toDTO(Chat entity) {
        return new ChatResponse(entity.getChatId(), entity.getChatName());
    }

    @Override
    public Collection<ChatResponse> allToDTO(Collection<Chat> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}
