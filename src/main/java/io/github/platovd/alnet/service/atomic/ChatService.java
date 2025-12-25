package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.exception.base.WrongDataException;
import io.github.platovd.alnet.exception.chat.ChatNotFoundException;
import io.github.platovd.alnet.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Сервис для работы с чатами.
 * Предоставляет основные CRUD операции для работы с сущностью Chat.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    /**
     * Репозиторий для работы с чатами.
     */
    private final ChatRepository repository;

    /**
     * Получает чат по его идентификатору.
     *
     * @param chatId идентификатор чата
     * @return найденный чат
     * @throws ChatNotFoundException если чат с указанным идентификатором не найден
     */
    @Transactional
    public Chat getChatById(Long chatId) {
        return repository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("No chat with this id"));
    }

    /**
     * Создает новый чат с указанным названием.
     *
     * @param name название чата
     * @return созданный чат
     */
    @Transactional
    public Chat createChat(String name) {
        Chat chat = Chat.builder().chatName(name).build();
        return repository.save(chat);
    }

    /**
     * Обновляет информацию о чате.
     *
     * @param chatId идентификатор чата из URL
     * @param request запрос с данными для обновления
     * @return обновленный чат
     * @throws WrongDataException если идентификатор в URL не совпадает с идентификатором в запросе
     */
    @Transactional
    public Chat updateChat(Long chatId, ChatCreationOrUpdateRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("DTO and url data isn't the same");
        Chat chat = getChatById(request.getChatId());
        chat.setChatName(request.getName());
        repository.save(chat);
        return chat;
    }

    /**
     * Удаляет чат по его идентификатору.
     *
     * @param chatId идентификатор чата для удаления
     */
    @Transactional
    public void deleteChatById(Long chatId) {
        repository.deleteById(chatId);
    }
}