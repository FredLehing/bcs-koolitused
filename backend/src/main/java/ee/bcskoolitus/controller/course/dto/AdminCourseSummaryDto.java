package ee.bcskoolitus.controller.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCourseSummaryDto {
    private Integer totalPages;
    private Long totalElements;
    private List<AdminCourseSummaryItemDto> adminCourseSummaries;
}
