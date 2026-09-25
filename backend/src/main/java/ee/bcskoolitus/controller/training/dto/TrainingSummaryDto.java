package ee.bcskoolitus.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingSummaryDto implements Serializable {
    private Integer totalPages;
    private Long totalElements;
    private List<TrainingSummaryItemDto> trainingSummaries;
}
