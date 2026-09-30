package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingDto {

    private Integer trainingId;
    private Integer categoryId;
    private Integer trainingLanguageId;
    private Integer locationId;
    private Integer defaultLecturerId;
    private String defaultLecturerName;
    private Boolean isOrderable;
    private Boolean isPromoted;
    private String status;
    private List<Integer> fundingTypeIds;
}