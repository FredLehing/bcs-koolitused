package ee.bcskoolitus.controller.lecturer.dto;

import ee.bcskoolitus.infrastructure.validation.HtmlNotBlank;
import ee.bcskoolitus.infrastructure.validation.ValidBase64;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerCreateRequestDto {

    @NotNull
    private Integer userId;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    // Pilt Base64 kujul või null; normaliseeritakse (400×400 JPEG)
    @ValidBase64
    private String photo;

    // image/png, image/jpeg või image/webp — kohustuslik, kui photo on antud
    private String photoContentType;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String shortDescription;

    @NotBlank
    @HtmlNotBlank
    private String description;
}
