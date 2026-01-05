package io.github.platovd.alnet.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Schema(description = "Объект переноса данных для чата")
@NotNull
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    @Max(value = Long.MAX_VALUE)
    private Long chatId;

    @Size(max = 30)
    private String name;

    @Size(max = 100)
    private List<String> members;
}