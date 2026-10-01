package ee.bcskoolitus.controller.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackAnswerDto {

    @NotNull
    private Integer feedbackCriteriaId;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer score;

    // Valikuline; tühi → null (FeedbackService)
    @Size(max = 255)
    private String feedbackText;
}
