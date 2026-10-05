package ee.bcskoolitus.controller.feedback.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Tagasiside lisamine ja muutmine: vastus iga vormi kriteeriumi kohta
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackRequestDto {

    @NotEmpty
    @Valid
    private List<FeedbackAnswerDto> answers;
}
