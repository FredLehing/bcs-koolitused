package ee.bcskoolitus.controller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Vormi üks kriteerium; score ja feedbackText on null, kui sellele pole vastatud
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCriteriaItemDto {
    private Integer feedbackCriteriaId;
    // contentLang keeles, puudumisel põhikeeles
    private String title;
    private String description;
    private Integer score;
    private String feedbackText;
}
