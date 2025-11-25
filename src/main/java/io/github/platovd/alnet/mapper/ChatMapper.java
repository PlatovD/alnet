package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.mapper.info.ChatInfo;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ChatMapper implements MapperFromEntityToInfo<Chat, ChatInfo> {
    @Override
    public ChatInfo toInfo(Chat entity) {
        return new ChatInfo(entity.getChatId(), entity.getChatName());
    }

    @Override
    public Collection<ChatInfo> allToInfo(Collection<Chat> entities) {
        return entities.stream().map(this::toInfo).toList();
    }
}
