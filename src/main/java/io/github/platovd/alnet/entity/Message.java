package io.github.platovd.alnet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "time")
    @CreationTimestamp
    private LocalDateTime dateTime;

    @ManyToOne(optional = false, targetEntity = Chat.class)
    private Chat chat;

    @ManyToOne(optional = false, targetEntity = User.class)
    private User user;
}
