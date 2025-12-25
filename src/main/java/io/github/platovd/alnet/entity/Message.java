package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая сообщение в чате.
 * Содержит текст сообщения, время отправки и информацию об авторе.
 *
 * @author PlatovD
 * @version 1.0
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "message")
public class Message {

    /**
     * Уникальный идентификатор сообщения.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    /**
     * Текст сообщения.
     * Максимальная длина - 2000 символов.
     */
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    /**
     * Дата и время создания сообщения.
     * Автоматически устанавливается при создании записи.
     */
    @Column(name = "time")
    @CreationTimestamp
    private LocalDateTime dateTime;

    /**
     * Чат, в котором находится сообщение.
     * Связь многие-к-одному с сущностью Chat.
     */
    @ManyToOne(optional = false, targetEntity = Chat.class)
    private Chat chat;

    /**
     * Пользователь-автор сообщения.
     * Связь многие-к-одному с сущностью User.
     */
    @ManyToOne(optional = false, targetEntity = User.class)
    private User user;
}
