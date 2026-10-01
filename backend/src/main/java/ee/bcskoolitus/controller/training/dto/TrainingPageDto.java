package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.common.dto.LecturerSummaryDto;
import ee.bcskoolitus.controller.common.dto.UpcomingCourseDto;
import lombok.Data;

import java.util.List;

// Koolituse detail ja vormist avatava tõlke eelvaade; PDF-i baite siin ei tagastata.
@Data
public class TrainingPageDto {
    private Integer trainingId;
    private Integer trainingTranslationId;
    private Boolean isMainLanguageFallback;
    private String title;
    private String shortDescription;
    private String description;
    private String categoryName;
    private String trainingLanguageCode;
    private String trainingLanguageFlagIconCode;
    private String locationName;
    private Boolean isOnline;
    private Boolean isOrderable;
    private Boolean isPromoted;
    private String curriculumFileName;
    private Integer curriculumFileSize;
    private List<FundingTypeDto> fundingTypes;
    private List<LecturerSummaryDto> lecturers;
    private List<UpcomingCourseDto> upcomingCourses;
}
