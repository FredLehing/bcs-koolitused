package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminTrainingSummaryItemDto {
    private Integer trainingId;
    // contentLang keele tõlge, puudumisel põhikeele tõlge — "Vaata" ja "Muuda" link avavad selle
    private Integer trainingTranslationId;
    private String title;
    private Integer categoryId;
    private String categoryName;
    private String trainingLanguageCode;
    private String trainingLanguageFlagIconCode;
    private String status;
    private Boolean isOrderable;
    private Boolean isPromoted;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean hasAllTranslations;
    private List<String> missingTranslationLanguageCodes;
    private List<FundingTypeDto> fundingTypes;
}
