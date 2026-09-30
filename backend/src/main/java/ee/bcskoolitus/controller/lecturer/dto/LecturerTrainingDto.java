package ee.bcskoolitus.controller.lecturer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerTrainingDto {

    private Integer trainingId;
    private Integer trainingTranslationId;
    private String title;
}
