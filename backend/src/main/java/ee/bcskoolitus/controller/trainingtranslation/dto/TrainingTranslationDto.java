package ee.bcskoolitus.controller.trainingtranslation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTranslationDto implements Serializable {

    private Integer trainingTranslationId;
    private Integer trainingId;
    private Integer languageId;
    private String languageCode;
    private String title;
    private String shortDescription;
    // Richtext (HTML) — salvestamisel HtmlSanitizer-iga puhastatud, tagastatakse muutmata
    private String description;
}
