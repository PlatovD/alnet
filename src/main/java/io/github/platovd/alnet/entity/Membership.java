package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность, представляющая членство пользователя в чате.
 * Связывает пользователей с чатами (многие-ко-многим через промежуточную таблицу).
 *
 * @author PlatovD
 * @version 1.0
 */
@Entity
@Table(name = "membership")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Membership {

    /**
     * Уникальный идентификатор записи о членстве.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_chat_id")
    private Long userChatId;

    /**
     * Пользователь, являющийся членом чата.
     * Связь многие-к-одному с сущностью User.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Чат, в котором состоит пользователь.
     * Связь многие-к-одному с сущностью Chat.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;
}
