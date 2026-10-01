package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCriteriaSummaryDto {
    private Integer feedbackCriteriaId;
    private String title;
    private Double averageScore;
    private Long answerCount;
}
