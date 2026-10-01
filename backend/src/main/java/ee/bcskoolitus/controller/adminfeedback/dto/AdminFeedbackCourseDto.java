package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackCourseDto {
    private Integer courseId;
    private Integer trainingId;
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
}
