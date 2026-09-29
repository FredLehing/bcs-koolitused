package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslation;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingMapper;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingTypeRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummary;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummaryRepository;
import ee.bcskoolitus.persistance.view.trainingsummary.TrainingSummarySpecifications;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingSummaryRepository trainingSummaryRepository;
    private final TrainingSummaryMapper trainingSummaryMapper;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;
    private final FundingTypeTranslationMapper fundingTypeTranslationMapper;
    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingTranslationRepository trainingTranslationRepository;
    private final TrainingTranslationMapper trainingTranslationMapper;
    private final TrainingFundingTypeRepository trainingFundingTypeRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LanguageService languageService;
    private final LocationService locationService;
    private final LecturerService lecturerService;
    private final FundingTypeService fundingTypeService;

    public TrainingSummaryDto findFilteredTrainings(Integer categoryId, Integer fundingTypeId, Integer limit, Integer page, Integer trainingLanguageId, String contentLang, String searchText) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("training.isPromoted"), Sort.Order.asc("title")));
        Specification<TrainingSummary> trainingSummarySpecification = createTrainingSummarySpecification(categoryId, fundingTypeId, trainingLanguageId, contentLang, searchText);
        Page<TrainingSummary> filteredTrainingSummaryPage = trainingSummaryRepository.findAll(trainingSummarySpecification, pageable);
        List<TrainingSummaryItemDto> trainingSummaryItemDtos = findAndCreateTrainingSummaryItemDtos(contentLang, filteredTrainingSummaryPage);
        return createTrainingSummaryDto(filteredTrainingSummaryPage, trainingSummaryItemDtos);
    }

    private static Specification<TrainingSummary> createTrainingSummarySpecification(Integer categoryId, Integer fundingTypeId, Integer trainingLanguageId, String contentLang, String searchText) {
        return Specification.allOf(
                TrainingSummarySpecifications.hasCategoryId(categoryId),
                TrainingSummarySpecifications.hasFundingTypeId(fundingTypeId),
                TrainingSummarySpecifications.hasTrainingLanguageId(trainingLanguageId),
                TrainingSummarySpecifications.hasTranslationLanguageCode(contentLang),
                TrainingSummarySpecifications.containsAllWords(searchText));
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

    public Training getValidTrainingBy(Integer trainingId) {
        return trainingRepository.findById(trainingId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingId", trainingId));
    }

    @Transactional(readOnly = true)
    public TrainingDto getTraining(Integer trainingId) {
        Training training = getValidTrainingBy(trainingId);

        TrainingDto trainingDto = trainingMapper.toTrainingDto(training);

        List<Integer> fundingTypeIds = trainingFundingTypeRepository
                .findAllByTraining_IdOrderByFundingType_IdAsc(trainingId)
                .stream()
                .map(trainingFundingType -> trainingFundingType.getFundingType().getId())
                .toList();

        trainingDto.setFundingTypeIds(fundingTypeIds);

        return trainingDto;
    }

    @Transactional(readOnly = true)
    public List<TrainingTranslationItemDto> getTrainingTranslations(Integer trainingId) {
        getValidTrainingBy(trainingId);

        List<TrainingTranslation> trainingTranslations =
                trainingTranslationRepository
                        .findAllByTraining_IdOrderByLanguage_IdAsc(trainingId);

        return trainingTranslationMapper
                .toTrainingTranslationItemDtos(trainingTranslations);
    }

    // Loob koolituse (status U), rahastustüüpide seosed ja põhikeele tõlke ühes transaktsioonis
    @Transactional
    public TrainingCreateResponseDto addTraining(TrainingCreateRequestDto trainingCreateRequestDto) {
        Training training = createTraining(trainingCreateRequestDto);
        trainingRepository.save(training);
        addTrainingFundingTypes(training, trainingCreateRequestDto.getFundingTypeIds());
        TrainingTranslation trainingTranslation = createMainLanguageTrainingTranslation(training, trainingCreateRequestDto);
        trainingTranslationRepository.save(trainingTranslation);
        return new TrainingCreateResponseDto(training.getId(), trainingTranslation.getId());
    }

    private Training createTraining(TrainingCreateRequestDto trainingCreateRequestDto) {
        Training training = trainingMapper.toTraining(trainingCreateRequestDto);
        training.setUser(userService.getValidUserBy(trainingCreateRequestDto.getUserId()));
        training.setCategory(categoryService.getValidCategoryBy(trainingCreateRequestDto.getCategoryId()));
        training.setLocation(locationService.getValidLocationBy(trainingCreateRequestDto.getLocationId()));
        training.setTrainingLanguage(languageService.getValidLanguageBy(trainingCreateRequestDto.getTrainingLanguageId(), "trainingLanguageId"));
        handleSetDefaultLecturer(training, trainingCreateRequestDto.getDefaultLecturerId());
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        return training;
    }

    private void handleSetDefaultLecturer(Training training, Integer defaultLecturerId) {
        if (defaultLecturerId != null) {
            training.setDefaultLecturer(lecturerService.getValidLecturerBy(defaultLecturerId, "defaultLecturerId"));
        }
    }

    // Duplikaadid eemaldatakse, muidu annaks training_funding_type_uq andmebaasi vea
    private void addTrainingFundingTypes(Training training, List<Integer> fundingTypeIds) {
        for (Integer fundingTypeId : new LinkedHashSet<>(fundingTypeIds)) {
            TrainingFundingType trainingFundingType = new TrainingFundingType();
            trainingFundingType.setTraining(training);
            trainingFundingType.setFundingType(fundingTypeService.getValidFundingTypeBy(fundingTypeId));
            trainingFundingTypeRepository.save(trainingFundingType);
        }
    }

    private TrainingTranslation createMainLanguageTrainingTranslation(Training training, TrainingCreateRequestDto trainingCreateRequestDto) {
        TrainingTranslation trainingTranslation = trainingTranslationMapper.toTrainingTranslation(trainingCreateRequestDto);
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(languageService.getMainLanguage());
        return trainingTranslation;
    }
}
