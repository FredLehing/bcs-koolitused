package ee.bcskoolitus.controller.adminfeedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackPageDto {
    private Integer page;
    private Integer totalPages;
    private Long totalElements;
    private Long needsReviewCount;
    private Long lowScoreFeedbackCount;
    private Double overallAverageScore;
    private CourseFeedbackResponseRateDto courseResponseRate;
    private List<FeedbackCriteriaSummaryDto> criteriaAverages;
    private List<AdminFeedbackSummaryDto> content;
}
