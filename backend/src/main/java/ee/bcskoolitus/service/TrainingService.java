package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryItemDto;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslation;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummary;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingSummaryRepository trainingSummaryRepository;
    private final TrainingSummaryMapper trainingSummaryMapper;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;
    private final FundingTypeTranslationMapper fundingTypeTranslationMapper;

    public TrainingSummaryDto findFilteredTrainings(Integer categoryId, Integer fundingTypeId, Integer limit, Integer page, String trainingLang, String contentLang) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<TrainingSummary> filteredTrainingSummaryPage = trainingSummaryRepository.findFilteredTrainingSummariesBy(categoryId, fundingTypeId, trainingLang, contentLang, pageable);
        List<TrainingSummaryItemDto> trainingSummaryItemDtos = findAndCreateTrainingSummaryItemDtos(contentLang, filteredTrainingSummaryPage);
        return createTrainingSummaryDto(filteredTrainingSummaryPage, trainingSummaryItemDtos);
    }

    private @NonNull List<TrainingSummaryItemDto> findAndCreateTrainingSummaryItemDtos(String contentLang, Page<TrainingSummary> filteredTrainingSummaryPage) {
        List<TrainingSummaryItemDto> trainingSummaryItemDtos = trainingSummaryMapper.toTrainingSummaryItemDtos(filteredTrainingSummaryPage.getContent());
        for (TrainingSummaryItemDto trainingSummaryItemDto : trainingSummaryItemDtos) {
            handleAddFundingTypes(trainingSummaryItemDto, contentLang);
        }
        return trainingSummaryItemDtos;
    }

    private void handleAddFundingTypes(TrainingSummaryItemDto trainingSummaryItemDto, String contentLang) {
        Integer trainingId = trainingSummaryItemDto.getTrainingId().intValue();
        List<FundingTypeTranslation> fundingTypeTranslations = fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(trainingId, contentLang);
        List<FundingTypeDto> fundingTypeDtos = fundingTypeTranslationMapper.toFundingTypeDtos(fundingTypeTranslations);
        trainingSummaryItemDto.setFundingTypes(fundingTypeDtos);
    }

    private static @NonNull TrainingSummaryDto createTrainingSummaryDto(Page<TrainingSummary> filteredTrainingSummaryPage, List<TrainingSummaryItemDto> trainingSummaryItemDtos) {
        TrainingSummaryDto trainingSummaryDto = new TrainingSummaryDto();
        trainingSummaryDto.setTotalPages(filteredTrainingSummaryPage.getTotalPages());
        trainingSummaryDto.setTotalElements(filteredTrainingSummaryPage.getTotalElements());
        trainingSummaryDto.setTrainingSummaries(trainingSummaryItemDtos);
        return trainingSummaryDto;
    }
}
