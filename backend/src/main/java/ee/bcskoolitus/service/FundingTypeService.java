package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.fundingtype.FundingTypeRepository;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingTypeService {

    private final FundingTypeRepository fundingTypeRepository;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;
    private final FundingTypeTranslationMapper fundingTypeTranslationMapper;

    public FundingType getValidFundingTypeBy(Integer fundingTypeId) {
        return fundingTypeRepository.findById(fundingTypeId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("fundingTypeId", fundingTypeId));
    }

    public List<FundingTypeDto> getFundingTypes(String contentLang) {
        return fundingTypeTranslationMapper.toFundingTypeDtos(
                fundingTypeTranslationRepository
                        .findAllByLanguage_CodeOrderByFundingType_IdAsc(contentLang)
        );
    }
}
