package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.fundingtype.FundingTypeRepository;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingTypeService {

    private final FundingTypeRepository fundingTypeRepository;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;

    public FundingType getValidFundingTypeBy(Integer fundingTypeId) {
        return fundingTypeRepository.findById(fundingTypeId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("fundingTypeId", fundingTypeId));
    }

    public void findFundingTypes(String contentLang) {
        fundingTypeTranslationRepository
    }
}
