package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "name")
    private String chatName;

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Membership> membership;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, targetEntity = Message.class, fetch = FetchType.LAZY)
    private List<Message> messages;
}
