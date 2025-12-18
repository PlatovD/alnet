package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.message.response.MessageResponse;
import io.github.platovd.alnet.entity.Message;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MessageMapper implements MapperFromEntityToDTO<Message, MessageResponse> {
    @Override
    public MessageResponse toDTO(Message entity) {
        return MessageResponse.builder()
                .id(entity.getMessageId())
                .chatId(entity.getChat().getChatId())
                .username(entity.getUser().getUsername())
                .content(entity.getContent())
                .dateTime(entity.getCreationTime()).build();
    }

    @Override
    public Collection<MessageResponse> allToDTO(Collection<Message> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}
