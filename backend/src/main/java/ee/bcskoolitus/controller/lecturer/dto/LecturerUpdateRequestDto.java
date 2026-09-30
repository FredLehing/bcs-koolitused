package ee.bcskoolitus.controller.lecturer.dto;

import ee.bcskoolitus.infrastructure.validation.ValidBase64;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerUpdateRequestDto {

    @NotBlank
    @Size(max = 255)
    private String fullName;

    // Uus pilt Base64 kujul (normaliseeritakse); null = pilti ei muudeta
    @ValidBase64
    private String photo;

    // image/png, image/jpeg või image/webp — kohustuslik, kui photo on antud
    private String photoContentType;

    // true = pilt eemaldatakse (photo peab siis olema null)
    private Boolean isPhotoRemoved;

    @NotNull
    @Valid
    private LecturerTranslationUpdateDto lecturerTranslation;

    @AssertTrue(message = "pilti ei saa korraga eemaldada ja lisada")
    private boolean isPhotoRemovedWithoutNewPhoto() {
        return !Boolean.TRUE.equals(isPhotoRemoved) || photo == null;
    }
}
