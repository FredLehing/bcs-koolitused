package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTitleDto {

    private Integer trainingId;
    private String title;
}
