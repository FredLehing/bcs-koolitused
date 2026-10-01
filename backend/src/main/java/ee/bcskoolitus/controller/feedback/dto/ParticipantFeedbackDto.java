package ee.bcskoolitus.controller.feedback.dto;

import ee.bcskoolitus.controller.common.dto.FeedbackCriteriaItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// Tagasiside vorm: päis ja kriteeriumid (koos vastustega, kui tagasiside on olemas)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantFeedbackDto {
    private Integer courseParticipantId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    // false → uus tagasiside (POST), true → olemasolev (PUT)
    private Boolean hasFeedback;
    // null, kui tagasisidet pole
    private Instant createdAt;
    // Vastuste viimane muutmise aeg; null, kui tagasisidet pole
    private Instant updatedAt;
    private List<FeedbackCriteriaItemDto> criteria;
}
