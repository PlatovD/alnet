package io.github.platovd.alnet.entity;

import io.github.platovd.alnet.entity.util.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность, представляющая пользователя системы.
 * Содержит основную информацию о пользователе и его связи с другими сущностями.
 *
 * @author PlatovD
 * @version 1.0
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "user")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * Имя пользователя для входа в систему.
     * Должно быть уникальным.
     */
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    /**
     * Хэшированный пароль пользователя.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Адрес электронной почты пользователя.
     * Должен быть уникальным.
     */
    @Column(name = "email", unique = true)
    private String email;

    /**
     * Статус пользователя в системе (онлайн/оффлайн).
     * По умолчанию устанавливается OFFLINE.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.OFFLINE;

    /**
     * Список ролей пользователя.
     * Определяет права доступа пользователя в системе.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private List<Role> role;

    /**
     * Список членств пользователя в чатах.
     * Связь один-ко-многим с сущностью Membership.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Membership> membership;
}