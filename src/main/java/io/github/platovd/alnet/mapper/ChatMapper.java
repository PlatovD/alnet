// todo: сделать единый dto
package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.chat.ChatDTO;
import io.github.platovd.alnet.entity.Chat;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ChatMapper implements MapperFromEntityToDTO<Chat, ChatDTO> {
    @Override
    public ChatDTO toDTO(Chat entity) {
        ChatDTO.ChatDTOBuilder builder = ChatDTO.builder()
                .chatId(entity.getChatId())
                .name(entity.getChatName());

        return builder.build();
    }

    @Override
    public Collection<ChatDTO> allToDTO(Collection<Chat> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}
