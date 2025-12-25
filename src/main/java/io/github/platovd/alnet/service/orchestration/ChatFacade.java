package io.github.platovd.alnet.service.orchestration;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.dto.membership.request.MembershipOperationRequest;
import io.github.platovd.alnet.dto.membership.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.dto.membership.response.UserMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.base.WrongDataException;
import io.github.platovd.alnet.mapper.ChatMapper;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Фасад для работы с чатами.
 * Координирует работу нескольких сервисов для выполнения сложных операций с чатами.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ChatFacade {

    /**
     * Сервис для работы с членством в чатах.
     */
    private final MembershipService membershipService;

    /**
     * Сервис для работы с чатами.
     */
    private final ChatService chatService;

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Маппер для преобразования чатов.
     */
    private final ChatMapper chatMapper;

    /**
     * Создает новый чат с указанными участниками.
     *
     * @param chatCreationOrUpdateRequest запрос с данными для создания чата
     * @return созданный чат
     */
    @Transactional
    public Chat createChatWithMembers(ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.createChat(chatCreationOrUpdateRequest.getName());
        List<User> users = userService.getAllUsersByUsername(chatCreationOrUpdateRequest.getMembers());
        membershipService.addAllMembersToChat(users, chat);
        return chat;
    }

    /**
     * Получает список чатов текущего пользователя.
     *
     * @return список чатов пользователя
     */
    public UserChatsMembershipResponse getChatsListForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        List<Membership> memberships = membershipService.getAllMembershipsForUser(currentUser);
        return UserChatsMembershipResponse.builder().username(currentUser.getUsername()).chats(
                chatMapper.allToDTO(memberships.stream().map(Membership::getChat).toList())
        ).build();
    }

    /**
     * Добавляет пользователей в указанный чат.
     *
     * @param chatId идентификатор чата из URL
     * @param request запрос с данными для добавления пользователей
     * @return информация о добавленных пользователях
     * @throws WrongDataException если идентификатор в URL не совпадает с идентификатором в запросе
     */
    public ChatMembersResponse addAllUsersToChat(Long chatId, MembershipOperationRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("Url param and request body have conflict information");
        Chat chat = chatService.getChatById(request.getChatId());
        List<User> usersToAdd = userService.getAllUsersByUsername(request.getUsernames());
        membershipService.addAllMembersToChat(usersToAdd, chat);
        return new ChatMembersResponse(chat.getChatId(), usersToAdd.stream().map(UserMembershipResponse::new).toList());
    }

    /**
     * Удаляет пользователей из указанного чата.
     *
     * @param chatId идентификатор чата из URL
     * @param request запрос с данными для удаления пользователей
     * @throws WrongDataException если идентификатор в URL не совпадает с идентификатором в запросе
     */
    public void deleteMembersOfChat(Long chatId, MembershipOperationRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("Url param and request body have conflict information");
        Chat chat = chatService.getChatById(request.getChatId());
        List<User> users = userService.getAllUsersByUsername(request.getUsernames());
        membershipService.removeAllMembersFromChat(users, chat);
    }
}