package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.infrastructure.validation.HtmlNotBlank;
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
}
