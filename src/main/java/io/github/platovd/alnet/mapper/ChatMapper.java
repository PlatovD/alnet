// todo: сделать единый dto
package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.chat.ChatDTO;
import io.github.platovd.alnet.entity.Chat;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Маппер для преобразования сущности Chat в ChatDTO и обратно.
 * Реализует интерфейс MapperFromEntityToDTO для работы с чатами.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
public class ChatMapper implements MapperFromEntityToDTO<Chat, ChatDTO> {

    /**
     * Преобразует сущность Chat в ChatDTO.
     *
     * @param entity сущность Chat для преобразования
     * @return объект ChatDTO с данными из сущности
     */
    @Override
    public ChatDTO toDTO(Chat entity) {
        ChatDTO.ChatDTOBuilder builder = ChatDTO.builder()
                .chatId(entity.getChatId())
                .name(entity.getChatName());

        return builder.build();
    }

    /**
     * Преобразует коллекцию сущностей Chat в коллекцию ChatDTO.
     *
     * @param entities коллекция сущностей Chat для преобразования
     * @return коллекция объектов ChatDTO
     */
    @Override
    public Collection<ChatDTO> allToDTO(Collection<Chat> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}