package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingSummaryItemDto implements Serializable {
    private Long trainingId;
    private String trainingLanguageCode;
    private String title;
    private String shortDescription;
    private Integer categoryId;
    private String categoryName;
    private Boolean isOrderOnly;
    private Boolean isPromoted;
    private List<FundingTypeDto> fundingTypes;
}
