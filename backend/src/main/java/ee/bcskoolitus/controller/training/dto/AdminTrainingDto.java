package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Koolituse admini ülevaade (kalendri kaart, toimumiskorra vormi pealkiri ja vaikimisi koolitajad)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminTrainingDto {

    private Integer trainingId;
    private Integer trainingTranslationId;
    private String title;
    private String description;
    private String categoryName;
    private String trainingLanguageCode;
    private String trainingLanguageFlagIconCode;
    private String locationName;
    private List<LecturerDto> lecturers;
    private String status;
    private Boolean isOrderable;
    private Boolean isPromoted;
    private List<FundingTypeDto> fundingTypes;
}
