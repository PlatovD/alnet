package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность, представляющая чат в системе.
 * Содержит информацию о чате и его участниках.
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
@Table(name = "chat")
public class Chat {

    /**
     * Уникальный идентификатор чата.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    /**
     * Название чата.
     */
    @Column(name = "name")
    private String chatName;

    /**
     * Список членств в чате.
     * Связь один-ко-многим с сущностью Membership.
     */
    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Membership> membership;

    /**
     * Список сообщений в чате.
     * Связь один-ко-многим с сущностью Message.
     */
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, targetEntity = Message.class, fetch = FetchType.LAZY)
    private List<Message> messages;
}