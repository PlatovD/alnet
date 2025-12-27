package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.exception.entity.chat.ChatException;
import io.github.platovd.alnet.exception.entity.chat.ChatNotFoundException;
import io.github.platovd.alnet.repository.ChatRepository;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository repository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void getChatByIdCorrectTest() {
        // given
        Chat testChat = FabricForTests.testChat();
        when(repository.findById(FabricForTests.CHAT_ID)).thenReturn(Optional.of(testChat));

        // when
        Chat result = chatService.getChatById(FabricForTests.CHAT_ID);

        // then
        assertThat(result).isEqualTo(testChat);
        verify(repository).findById(FabricForTests.CHAT_ID);
    }

    @Test
    void getChatByIdShouldThrowChatNotFoundExceptionTest() {
        // given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.getChatById(FabricForTests.CHAT_ID))
                .isInstanceOf(ChatNotFoundException.class)
                .hasMessage("No chat with this id");

        verify(repository).findById(FabricForTests.CHAT_ID);
    }

    @Test
    void createChatCorrectTest() {
        // given
        Chat savedChat = FabricForTests.testChat();
        when(repository.save(any(Chat.class))).thenReturn(savedChat);

        // when
        Chat result = chatService.createChat(FabricForTests.CHAT_NAME);

        // then
        assertThat(result).isEqualTo(savedChat);
        verify(repository).save(argThat(chat ->
                chat.getChatName().equals(FabricForTests.CHAT_NAME)
        ));
    }

    @Test
    void createChatWithEmptyNameTest() {
        // given
        String emptyName = "";
        Chat savedChat = Chat.builder()
                .chatId(FabricForTests.CHAT_ID)
                .chatName(emptyName)
                .build();

        when(repository.save(any(Chat.class))).thenReturn(savedChat);

        // when
        Chat result = chatService.createChat(emptyName);

        // then
        assertThat(result.getChatName()).isEmpty();
        verify(repository).save(argThat(chat ->
                chat.getChatName().isEmpty()
        ));
    }

    @Test
    void updateChatCorrectTest() {
        // given
        Chat existingChat = FabricForTests.testChat();
        ChatCreationOrUpdateRequest request = FabricForTests.chatUpdateRequest();
        Chat updatedChat = Chat.builder()
                .chatId(FabricForTests.CHAT_ID)
                .chatName(FabricForTests.UPDATED_CHAT_NAME)
                .build();

        when(repository.findById(FabricForTests.CHAT_ID)).thenReturn(Optional.of(existingChat));
        when(repository.save(any(Chat.class))).thenReturn(updatedChat);

        // when
        Chat result = chatService.updateChat(FabricForTests.CHAT_ID, request);

        // then
        assertThat(result.getChatName()).isEqualTo(FabricForTests.UPDATED_CHAT_NAME);
        verify(repository).findById(FabricForTests.CHAT_ID);
        verify(repository).save(argThat(chat ->
                chat.getChatName().equals(FabricForTests.UPDATED_CHAT_NAME)
        ));
    }

    @Test
    void updateChatShouldThrowWrongDataExceptionTest() {
        // given
        ChatCreationOrUpdateRequest request = FabricForTests.chatUpdateRequestWithDifferentId();

        // when & then
        assertThatThrownBy(() -> chatService.updateChat(FabricForTests.CHAT_ID, request))
                .isInstanceOf(ChatException.class)
                .hasMessage("DTO and url data isn't the same");

        verify(repository, never()).findById(anyLong());
        verify(repository, never()).save(any(Chat.class));
    }

    @Test
    void updateChatShouldThrowChatNotFoundExceptionTest() {
        // given
        ChatCreationOrUpdateRequest request = FabricForTests.chatUpdateRequest();
        when(repository.findById(FabricForTests.CHAT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.updateChat(FabricForTests.CHAT_ID, request))
                .isInstanceOf(ChatNotFoundException.class)
                .hasMessage("No chat with this id");

        verify(repository).findById(FabricForTests.CHAT_ID);
        verify(repository, never()).save(any(Chat.class));
    }

    @Test
    void deleteChatByIdCorrectTest() {
        // when
        chatService.deleteChatById(FabricForTests.CHAT_ID);

        // then
        verify(repository).deleteById(FabricForTests.CHAT_ID);
    }

    @Test
    void deleteChatByIdWithZeroIdTest() {
        // given
        Long zeroId = 0L;

        // when
        chatService.deleteChatById(zeroId);

        // then
        verify(repository).deleteById(zeroId);
    }

    @Test
    void deleteChatByIdWithNegativeIdTest() {
        // given
        Long negativeId = -1L;

        // when
        chatService.deleteChatById(negativeId);

        // then
        verify(repository).deleteById(negativeId);
    }
}
