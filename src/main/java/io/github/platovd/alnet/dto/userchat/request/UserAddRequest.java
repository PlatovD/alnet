package io.github.platovd.alnet.dto.userchat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Запрос на добавление пользователя в чат")
public class UserAddRequest {
    @Schema(name = "Идентификатор чата")
    private Long chatId;

    @Schema(name = "Имя пользователя")
    private String username;
}
