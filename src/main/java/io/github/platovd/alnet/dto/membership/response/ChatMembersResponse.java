package io.github.platovd.alnet.dto.membership.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Список пользователей данного чата")
@NotNull
public class ChatMembersResponse {
    @Min(1)
    @Max(Long.MAX_VALUE)
    private Long chatId;

    @NotNull
    @NotEmpty
    private List<UserMembershipResponse> members;
}
