package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.repository.MembershipRepository;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository repository;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void addUserAsMemberCorrectTest() {
        // given
        User user = FabricForTests.testUser();
        Chat chat = FabricForTests.testChat();
        when(repository.existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId()))
                .thenReturn(false);

        // when
        membershipService.addUserAsMember(user, chat);

        // then
        verify(repository).existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
        verify(repository).save(argThat(membership ->
                membership.getUser().equals(user) &&
                        membership.getChat().equals(chat)
        ));
    }

    @Test
    void addUserAsMemberWhenAlreadyMemberTest() {
        // given
        User user = FabricForTests.testUser();
        Chat chat = FabricForTests.testChat();
        when(repository.existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId()))
                .thenReturn(true);

        // when
        membershipService.addUserAsMember(user, chat);

        // then
        verify(repository).existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
        verify(repository, never()).save(any(Membership.class));
    }

    @Test
    void addAllMembersToChatCorrectTest() {
        // given
        List<User> users = FabricForTests.testUserList();
        Chat chat = FabricForTests.testChat();
        when(repository.getAllExistingInChatUsersIds(anyList(), eq(chat.getChatId())))
                .thenReturn(Set.of());

        // when
        membershipService.addAllMembersToChat(users, chat);

        // then
        verify(repository).getAllExistingInChatUsersIds(
                eq(List.of(users.get(0).getUserId(), users.get(1).getUserId())),
                eq(chat.getChatId())
        );
    }

    @Test
    void addAllMembersToChatWithExistingMembersTest() {
        // given
        List<User> users = FabricForTests.testUserList();
        Chat chat = FabricForTests.testChat();
        when(repository.getAllExistingInChatUsersIds(anyList(), eq(chat.getChatId())))
                .thenReturn(Set.of(users.get(0).getUserId()));

        // when
        membershipService.addAllMembersToChat(users, chat);

        // then
        verify(repository).getAllExistingInChatUsersIds(anyList(), eq(chat.getChatId()));
    }

    @Test
    void addAllMembersToChatWithEmptyUsersListTest() {
        // given
        List<User> emptyUsers = List.of();
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.addAllMembersToChat(emptyUsers, chat);

        // then
        verify(repository, never()).getAllExistingInChatUsersIds(anyList(), anyLong());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void addAllMembersToChatWithNullUsersTest() {
        // given
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.addAllMembersToChat(null, chat);

        // then
        verify(repository, never()).getAllExistingInChatUsersIds(anyList(), anyLong());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void getAllMembershipsForUserCorrectTest() {
        // given
        User user = FabricForTests.testUser();
        List<Membership> expectedMemberships = List.of(FabricForTests.testMembership());
        when(repository.getAllByUserUserId(user.getUserId()))
                .thenReturn(expectedMemberships);

        // when
        List<Membership> result = membershipService.getAllMembershipsForUser(user);

        // then
        assertThat(result).isEqualTo(expectedMemberships);
        verify(repository).getAllByUserUserId(user.getUserId());
    }

    @Test
    void getAllMembersOfChatCorrectTest() {
        // given
        Long chatId = FabricForTests.CHAT_ID;
        List<User> expectedUsers = FabricForTests.testUserList();
        when(repository.getAllMembersOfChatByChatId(chatId))
                .thenReturn(expectedUsers);

        // when
        List<User> result = membershipService.getAllMembersOfChat(chatId);

        // then
        assertThat(result).isEqualTo(expectedUsers);
        verify(repository).getAllMembersOfChatByChatId(chatId);
    }

    @Test
    void removeMemberFromChatCorrectTest() {
        // given
        User user = FabricForTests.testUser();
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.removeMemberFromChat(user, chat);

        // then
        verify(repository).deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    @Test
    void removeAllMembersFromChatCorrectTest() {
        // given
        List<User> users = FabricForTests.testUserList();
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.removeAllMembersFromChat(users, chat);

        // then
        verify(repository).removeAllByUserUserIdAndChatChatId(
                eq(List.of(users.get(0).getUserId(), users.get(1).getUserId())),
                eq(chat.getChatId())
        );
    }

    @Test
    void removeAllMembersFromChatWithEmptyUsersListTest() {
        // given
        List<User> emptyUsers = List.of();
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.removeAllMembersFromChat(emptyUsers, chat);

        // then
        verify(repository, never()).removeAllByUserUserIdAndChatChatId(anyList(), anyLong());
    }

    @Test
    void removeAllMembersFromChatWithNullUsersTest() {
        // given
        Chat chat = FabricForTests.testChat();

        // when
        membershipService.removeAllMembersFromChat(null, chat);

        // then
        verify(repository, never()).removeAllByUserUserIdAndChatChatId(anyList(), anyLong());
    }

    @Test
    void isMemberOfChatCorrectTest() {
        // given
        Long userId = FabricForTests.USER_ID;
        Long chatId = FabricForTests.CHAT_ID;
        when(repository.existsByUserUserIdAndChatChatId(userId, chatId))
                .thenReturn(true);

        // when
        boolean result = membershipService.isMemberOfChat(userId, chatId);

        // then
        assertThat(result).isTrue();
        verify(repository).existsByUserUserIdAndChatChatId(userId, chatId);
    }

    @Test
    void isMemberOfChatWhenNotMemberTest() {
        // given
        Long userId = FabricForTests.USER_ID;
        Long chatId = FabricForTests.CHAT_ID;
        when(repository.existsByUserUserIdAndChatChatId(userId, chatId))
                .thenReturn(false);

        // when
        boolean result = membershipService.isMemberOfChat(userId, chatId);

        // then
        assertThat(result).isFalse();
        verify(repository).existsByUserUserIdAndChatChatId(userId, chatId);
    }

    @Test
    void getCountMembersOfChatCorrectTest() {
        // given
        Long chatId = FabricForTests.CHAT_ID;
        Long expectedCount = 5L;
        when(repository.countMembersOfChat(chatId))
                .thenReturn(expectedCount);

        // when
        Long result = membershipService.getCountMembersOfChat(chatId);

        // then
        assertThat(result).isEqualTo(expectedCount);
        verify(repository).countMembersOfChat(chatId);
    }

    @Test
    void getCountMembersOfChatWithZeroMembersTest() {
        // given
        Long chatId = FabricForTests.CHAT_ID;
        when(repository.countMembersOfChat(chatId))
                .thenReturn(0L);

        // when
        Long result = membershipService.getCountMembersOfChat(chatId);

        // then
        assertThat(result).isZero();
        verify(repository).countMembersOfChat(chatId);
    }

    @Test
    void getCountMembersOfChatWithNegativeChatIdTest() {
        // given
        Long negativeChatId = -1L;
        when(repository.countMembersOfChat(negativeChatId))
                .thenReturn(0L);

        // when
        Long result = membershipService.getCountMembersOfChat(negativeChatId);

        // then
        assertThat(result).isZero();
        verify(repository).countMembersOfChat(negativeChatId);
    }

    @Test
    void addUserAsMemberWithNullUserTest() {
        // given
        Chat chat = FabricForTests.testChat();

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            membershipService.addUserAsMember(null, chat);
        });
    }

    @Test
    void addUserAsMemberWithNullChatTest() {
        // given
        User user = FabricForTests.testUser();

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            membershipService.addUserAsMember(user, null);
        });
    }
}
