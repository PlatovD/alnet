package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.mapper.info.ChatInfoDTO;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ChatMapper implements MapperFromEntityToInfo<Chat, ChatInfoDTO> {
    @Override
    public ChatInfoDTO toInfo(Chat entity) {
        return new ChatInfoDTO(entity.getChatId(), entity.getChatName());
    }

    @Override
    public Collection<ChatInfoDTO> allToInfo(Collection<Chat> entities) {
        return entities.stream().map(this::toInfo).toList();
    }
}
