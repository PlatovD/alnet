package io.github.platovd.alnet.dto.membership.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на операцию с пользователем чата")
@NotNull
public class MembershipOperationRequest {
    private Long chatId;

    @Size(min = 1, max = 100)
    private List<String> usernames;
}
