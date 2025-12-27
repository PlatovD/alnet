package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Message;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.entity.message.MessageContentException;
import io.github.platovd.alnet.exception.entity.message.MessageNotFoundException;
import io.github.platovd.alnet.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository repository;

    @Transactional
    public Message createMessage(String content, User user, Chat chat) {
        if (content.isEmpty()) throw new MessageContentException("Message content mustn't be empty");
        Message message = Message.builder().content(content).user(user).chat(chat).build();
        return repository.save(message);
    }

    @Transactional(readOnly = true)
    public Slice<Message> getAllChatMessages(Long chatId, Pageable pageable) {
        // на фронте надо переворачивать
        return repository.getAllMessagesOfChatPaginated(chatId, pageable);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        repository.deleteById(messageId);
    }

    @Transactional
    public Message updateMessageContent(Long messageId, String newContent) {
        if (newContent.isEmpty()) throw new MessageContentException("Message content mustn't be empty");
        Message message = repository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message wasn't found"));
        message.setContent(newContent);
        return message;
    }

    @Transactional
    public User getMessageAuthor(Long messageId) {
        Optional<Message> message = repository.findById(messageId);
        return message.map(Message::getUser).orElse(null);
    }
}
