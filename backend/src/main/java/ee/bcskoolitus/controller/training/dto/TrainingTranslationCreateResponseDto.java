package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTranslationCreateResponseDto implements Serializable {
    private Integer trainingTranslationId;
}
