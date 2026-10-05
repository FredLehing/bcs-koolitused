package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import ee.bcskoolitus.controller.common.dto.FeedbackCriteriaItemDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackDto {
    private Integer feedbackId;
    private Integer courseParticipantId;
    private Integer courseId;
    private Integer trainingId;
    private String trainingTitle;
    private String participantName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Instant createdAt;
    private Instant answersUpdatedAt;
    private String answersVersion;
    private List<FeedbackCriteriaItemDto> criteria;
}
