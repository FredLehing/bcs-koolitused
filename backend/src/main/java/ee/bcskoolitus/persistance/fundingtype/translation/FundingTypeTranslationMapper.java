package ee.bcskoolitus.persistance.fundingtype.translation;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FundingTypeTranslationMapper {

    @Mapping(source = "fundingType.id", target = "fundingTypeId")
    @Mapping(source = "name", target = "fundingTypeName")
    FundingTypeDto toFundingTypeDto(FundingTypeTranslation fundingTypeTranslation);

    List<FundingTypeDto> toFundingTypeDtos(List<FundingTypeTranslation> fundingTypeTranslations);

}
