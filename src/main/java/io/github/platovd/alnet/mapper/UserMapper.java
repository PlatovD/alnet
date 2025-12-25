package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.user.UserDTO;
import io.github.platovd.alnet.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Маппер для преобразования сущности User в UserDTO.
 * Реализует интерфейс MapperFromEntityToDTO для работы с пользователями.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
public class UserMapper implements MapperFromEntityToDTO<User, UserDTO> {

    /**
     * Преобразует сущность User в UserDTO.
     *
     * @param entity сущность User для преобразования
     * @return объект UserDTO с данными из сущности
     */
    @Override
    public UserDTO toDTO(User entity) {
        return new UserDTO(entity.getUserId(), entity.getUsername(), entity.getStatus(), entity.getEmail());
    }

    /**
     * Преобразует коллекцию сущностей User в коллекцию UserDTO.
     *
     * @param entities коллекция сущностей User для преобразования
     * @return коллекция объектов UserDTO
     */
    @Override
    public Collection<UserDTO> allToDTO(Collection<User> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}