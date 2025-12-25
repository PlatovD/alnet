package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность, представляющая роль пользователя в системе.
 * Определяет уровень доступа и привилегии пользователя.
 *
 * @author PlatovD
 * @version 1.0
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "role")
public class Role {

    /**
     * Название роли.
     * Используется как первичный ключ, должно быть уникальным.
     */
    @Id
    @Column(name = "role_name", nullable = false, unique = true)
    private String name;

    /**
     * Список пользователей, имеющих данную роль.
     * Связь многие-ко-многим с сущностью User.
     */
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "role")
    private List<User> usersWithRole;
}