package ee.bcskoolitus.controller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingCourseDto {
    private Integer courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Boolean isOnSite;
    private Boolean isOnline;
}
