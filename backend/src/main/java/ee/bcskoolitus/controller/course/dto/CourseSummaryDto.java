package ee.bcskoolitus.controller.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSummaryDto {

    private Integer courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private Integer numberOfAcademicHours;
    private BigDecimal price;
    // Koolitajate nimed komadega sort_order järjekorras; null = koolitajaid pole
    private String lecturerNames;
    private String roomName;
    private String status;
    private Boolean isPast;
    private Long participantCount;
    private Boolean hasNotes;
    private Boolean hasMeetingLink;
}
