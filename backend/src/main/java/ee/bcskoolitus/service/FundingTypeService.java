package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.fundingtype.FundingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingTypeService {

    private final FundingTypeRepository fundingTypeRepository;

    public FundingType getValidFundingTypeBy(Integer fundingTypeId) {
        return fundingTypeRepository.findById(fundingTypeId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("fundingTypeId", fundingTypeId));
    }
}
