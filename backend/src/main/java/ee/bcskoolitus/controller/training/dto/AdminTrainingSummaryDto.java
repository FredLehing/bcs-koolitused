package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminTrainingSummaryDto {
    private Integer totalPages;
    private Long totalElements;
    private List<AdminTrainingSummaryItemDto> adminTrainingSummaries;
}
