package ee.bcskoolitus.controller.course.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Avaliku kalendri rida; veebilinki ei tagastata
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicCourseSummaryItemDto {
    private Integer courseId;
    private Integer trainingId;
    private String title;
    private String shortDescription;
    private String categoryName;
    private String trainingLanguageFlagIconCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private Integer numberOfAcademicHours;
    private BigDecimal price;
    private String status;
    private Boolean isPromoted;
    private Boolean isOnSite;
    private Boolean isOnline;
    // Koolitajad sort_order järjekorras; null = koolitajaid pole
    private String lecturerNames;
    private List<FundingTypeDto> fundingTypes;
}
