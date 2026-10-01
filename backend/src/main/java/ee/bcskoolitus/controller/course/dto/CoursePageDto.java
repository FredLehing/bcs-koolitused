package ee.bcskoolitus.controller.course.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Avalik toimumiskorra leht; veebilinki ei tagastata
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePageDto {
    private Integer courseId;
    private Integer trainingId;
    private Integer trainingTranslationId;
    // true = contentLang tõlge puudub, tekstid on põhikeeles
    private Boolean isMainLanguageFallback;
    private String title;
    private String shortDescription;
    private String description;
    private String categoryName;
    private String trainingLanguageFlagIconCode;
    private List<FundingTypeDto> fundingTypes;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPast;
    private Integer numberOfDays;
    private Integer numberOfAcademicHours;
    private BigDecimal price;
    private String status;
    private Boolean isOnSite;
    private Boolean isOnline;
    private List<LecturerDto> lecturers;
    // Sama koolituse avalikud tulevased toimumiskorrad alguse järgi (praegune kaasa arvatud, kui tulevane)
    private List<UpcomingCourseDto> upcomingCourses;
}
