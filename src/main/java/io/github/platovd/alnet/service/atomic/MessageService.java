package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Message;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.base.NotFoundException;
import io.github.platovd.alnet.exception.base.WrongDataException;
import io.github.platovd.alnet.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для работы с сообщениями.
 * Предоставляет операции для создания, получения, обновления и удаления сообщений.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    /**
     * Репозиторий для работы с сообщениями.
     */
    private final MessageRepository repository;

    /**
     * Создает новое сообщение.
     *
     * @param content содержание сообщения
     * @param user автор сообщения
     * @param chat чат в котором создается сообщение
     * @return созданное сообщение
     * @throws WrongDataException если содержание сообщения пустое
     */
    @Transactional
    public Message createMessage(String content, User user, Chat chat) {
        if (content.isEmpty()) throw new WrongDataException("Message content mustn't be empty");
        Message message = Message.builder().content(content).user(user).chat(chat).build();
        return repository.save(message);
    }

    /**
     * Получает все сообщения из указанного чата с пагинацией.
     *
     * @param chatId идентификатор чата
     * @param pageable параметры пагинации
     * @return срез сообщений с пагинацией
     */
    @Transactional(readOnly = true)
    public Slice<Message> getAllChatMessages(Long chatId, Pageable pageable) {
        // на фронте надо переворачивать
        return repository.getAllMessagesOfChatPaginated(chatId, pageable);
    }

    /**
     * Удаляет сообщение по его идентификатору.
     *
     * @param messageId идентификатор сообщения
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        repository.deleteById(messageId);
    }

    /**
     * Обновляет содержание сообщения.
     *
     * @param messageId идентификатор сообщения
     * @param newContent новое содержание сообщения
     * @return обновленное сообщение
     * @throws WrongDataException если новое содержание пустое
     * @throws NotFoundException если сообщение не найдено
     */
    @Transactional
    public Message updateMessageContent(Long messageId, String newContent) {
        if (newContent.isEmpty()) throw new WrongDataException("Message content mustn't be empty");
        Message message = repository.findById(messageId).orElseThrow(() -> new NotFoundException("Message wasn't found"));
        message.setContent(newContent);
        return message;
    }

    /**
     * Получает автора сообщения.
     *
     * @param messageId идентификатор сообщения
     * @return автор сообщения или null если сообщение не найдено
     */
    @Transactional
    public User getMessageAuthor(Long messageId) {
        Optional<Message> message = repository.findById(messageId);
        return message.map(Message::getUser).orElse(null);
    }
}