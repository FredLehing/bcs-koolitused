package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTranslationItemDto {

    private Integer trainingTranslationId;
    private Integer languageId;
    private String languageCode;
    private Boolean isMainLanguage;
}