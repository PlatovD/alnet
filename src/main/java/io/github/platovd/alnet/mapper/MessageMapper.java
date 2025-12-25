package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.message.response.MessageResponse;
import io.github.platovd.alnet.entity.Message;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Маппер для преобразования сущности Message в MessageResponse.
 * Реализует интерфейс MapperFromEntityToDTO для работы с сообщениями.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
public class MessageMapper implements MapperFromEntityToDTO<Message, MessageResponse> {

    /**
     * Преобразует сущность Message в MessageResponse.
     *
     * @param entity сущность Message для преобразования
     * @return объект MessageResponse с данными из сущности
     */
    @Override
    public MessageResponse toDTO(Message entity) {
        return MessageResponse.builder()
                .id(entity.getMessageId())
                .chatId(entity.getChat().getChatId())
                .username(entity.getUser().getUsername())
                .content(entity.getContent())
                .dateTime(entity.getDateTime()).build();
    }

    /**
     * Преобразует коллекцию сущностей Message в коллекцию MessageResponse.
     *
     * @param entities коллекция сущностей Message для преобразования
     * @return коллекция объектов MessageResponse
     */
    @Override
    public Collection<MessageResponse> allToDTO(Collection<Message> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}