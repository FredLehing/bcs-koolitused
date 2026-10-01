package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.infrastructure.validation.HtmlNotBlank;
import ee.bcskoolitus.infrastructure.validation.ValidBase64;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTranslationCreateRequestDto implements Serializable {
    // Sihtkeel; koolitus tuleb path'ist
    @NotNull
    private Integer languageId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String shortDescription;

    @NotBlank
    @HtmlNotBlank
    private String description;

    // Uus õppekava (PDF) Base64 kujul; null = faili ei lisata / ei muudeta
    @ValidBase64
    private String curriculum;

    // Sõna "õppekava" tõlke keeles (frontendi i18n-ist) — läheb failinimesse
    @NotBlank
    @Size(max = 50)
    private String curriculumLabel;
}
