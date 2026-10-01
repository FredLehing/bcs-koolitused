package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.infrastructure.validation.HtmlNotBlank;
import ee.bcskoolitus.infrastructure.validation.ValidBase64;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingUpdateRequestDto implements Serializable {
    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer trainingLanguageId;

    @NotNull
    private Integer locationId;

    // Koolitajad järjekorras (sort_order = indeks + 1); tühi list = koolitajaid pole
    @NotNull
    @UniqueElements(message = "ei tohi sisaldada korduvaid väärtusi")
    private List<@NotNull Integer> lecturerIds;

    @NotNull
    private Boolean isOrderable;

    @NotNull
    private Boolean isPromoted;

    @NotNull
    private List<Integer> fundingTypeIds;

    // Vormis avatud tõlge — peab kuuluma path'is olevale koolitusele
    @NotNull
    private Integer trainingTranslationId;

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

    // true = õppekava eemaldatakse (curriculum peab siis olema null)
    private Boolean isCurriculumRemoved;

    // Sõna "õppekava" tõlke keeles (frontendi i18n-ist) — läheb failinimesse
    @NotBlank
    @Size(max = 50)
    private String curriculumLabel;

    @AssertTrue(message = "õppekava ei saa korraga eemaldada ja lisada")
    private boolean isCurriculumRemovedWithoutNewCurriculum() {
        return !Boolean.TRUE.equals(isCurriculumRemoved) || curriculum == null;
    }
}
