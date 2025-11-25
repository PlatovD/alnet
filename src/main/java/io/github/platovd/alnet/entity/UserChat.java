package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_chat")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_chat_id")
    private Long userChatId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;
}
