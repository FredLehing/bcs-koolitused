package ee.bcskoolitus.controller.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// GET /api/courses vastus (avalik kalender); CourseSummaryDto on koolituse kalendri rida
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSummaryPageDto {
    private Integer totalPages;
    private Long totalElements;
    private List<PublicCourseSummaryItemDto> courseSummaries;
}
