package ee.bcskoolitus.controller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundingTypeDto {
    private Integer fundingTypeId;
    private String fundingTypeName;
}
