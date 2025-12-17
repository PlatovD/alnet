package io.github.platovd.alnet.mapper;

import io.github.platovd.alnet.dto.user.UserDTO;
import io.github.platovd.alnet.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UserMapper implements MapperFromEntityToDTO<User, UserDTO> {
    @Override
    public UserDTO toDTO(User entity) {
        return new UserDTO(entity.getUserId(), entity.getUsername(), entity.getEmail());
    }

    @Override
    public Collection<UserDTO> allToDTO(Collection<User> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}
