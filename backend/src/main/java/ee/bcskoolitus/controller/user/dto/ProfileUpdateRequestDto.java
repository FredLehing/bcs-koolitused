package ee.bcskoolitus.controller.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// "Minu andmed": e-post muudab nii profiili kui ka konto (sisselogimise) e-posti
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDto {

    @NotBlank
    @Size(max = 255)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    private String lastName;

    // Unikaalne tõstutundetult teiste kontode seas (kontrollib UserService → 403 EMAIL_TAKEN)
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;
}
