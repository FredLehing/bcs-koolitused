package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseFeedbackResponseRateDto {
    private Integer courseId;
    private Long registeredCount;
    private Long respondedCount;
    private Double responsePercentage;
}
