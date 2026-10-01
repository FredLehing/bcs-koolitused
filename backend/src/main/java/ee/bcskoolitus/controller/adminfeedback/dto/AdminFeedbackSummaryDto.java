package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackSummaryDto {
    private Integer feedbackId;
    private Integer courseParticipantId;
    private Integer courseId;
    private Integer trainingId;
    private String trainingTitle;
    private String participantName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant answersUpdatedAt;
    private Double averageScore;
    private Integer minimumScore;
    private Long commentCount;
    private String status;
}
