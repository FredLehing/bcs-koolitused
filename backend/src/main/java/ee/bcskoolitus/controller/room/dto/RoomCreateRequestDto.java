package ee.bcskoolitus.controller.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateRequestDto {

    @NotNull
    private Integer userId;

    @NotBlank
    @Size(max = 255)
    private String roomName;
}
