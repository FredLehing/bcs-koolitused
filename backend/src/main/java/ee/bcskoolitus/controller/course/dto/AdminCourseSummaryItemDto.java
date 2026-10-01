package ee.bcskoolitus.controller.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCourseSummaryItemDto {
    private Integer courseId;
    private Integer trainingId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPast;
    private Integer numberOfDays;
    private BigDecimal price;
    private String status;
    private Boolean isPromoted;
    private Boolean hasMeetingLink;
    // Ainult registreerunud (R) osalejad
    private Long participantCount;
    private Long paidCount;
    // Kõik toimumiskorraga seotud päringud
    private Long enquiryCount;
}
