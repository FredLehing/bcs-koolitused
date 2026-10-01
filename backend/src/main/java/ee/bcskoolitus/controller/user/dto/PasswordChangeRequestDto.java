package ee.bcskoolitus.controller.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequestDto {

    // Vale praegune parool → 403 INCORRECT_PASSWORD (UserService)
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    private String newPassword;
}
