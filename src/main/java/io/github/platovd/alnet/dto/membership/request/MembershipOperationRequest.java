package io.github.platovd.alnet.dto.membership.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Запрос на операцию с пользователем чата")
public class MembershipOperationRequest {
    @Schema(name = "Идентификатор чата")
    private Long chatId;

    @Schema(name = "Имя пользователя")
    private List<String> usernames;
}
