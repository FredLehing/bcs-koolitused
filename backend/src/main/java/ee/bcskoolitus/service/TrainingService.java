package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.infrastructure.util.HtmlSanitizer;
import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingFilterDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingTitleDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslation;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingMapper;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingTypeRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummarySpecifications;
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
import java.util.Map;

import static ee.bcskoolitus.Error.TRAINING_DELETED;
import static ee.bcskoolitus.Error.TRANSLATION_EXISTS;

@Service
@RequiredArgsConstructor
public class TrainingService {

    // GET /api/admin-trainings sortBy väärtus → AdminTrainingSummary väli (staatus töövoo järjekorras U → P → D)
    private static final Map<String, String> ADMIN_TRAINING_SORT_PROPERTIES = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "title", "title",
            "categoryName", "categoryName",
            "trainingLanguageCode", "trainingLanguageCode",
            "status", "statusOrder",
            "hasAllTranslations", "hasAllTranslations");
    private static final String DEFAULT_ADMIN_TRAINING_SORT_PROPERTY = "createdAt";

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
    private final TrainingTranslationService trainingTranslationService;
    private final AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    private final AdminTrainingSummaryMapper adminTrainingSummaryMapper;

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

    public AdminTrainingSummaryDto findAdminTrainings(AdminTrainingFilterDto adminTrainingFilterDto) {
        Pageable pageable = PageRequest.of(adminTrainingFilterDto.getPage(), adminTrainingFilterDto.getLimit(),
                createAdminTrainingSort(adminTrainingFilterDto.getSortBy(), adminTrainingFilterDto.getSortDirection()));
        Specification<AdminTrainingSummary> adminTrainingSummarySpecification = createAdminTrainingSummarySpecification(adminTrainingFilterDto);
        Page<AdminTrainingSummary> adminTrainingSummaryPage = adminTrainingSummaryRepository.findAll(adminTrainingSummarySpecification, pageable);
        List<AdminTrainingSummaryItemDto> adminTrainingSummaryItemDtos = findAndCreateAdminTrainingSummaryItemDtos(adminTrainingFilterDto.getContentLang(), adminTrainingSummaryPage);
        return new AdminTrainingSummaryDto(adminTrainingSummaryPage.getTotalPages(), adminTrainingSummaryPage.getTotalElements(), adminTrainingSummaryItemDtos);
    }

    // Tundmatu sortBy → createdAt; sortDirection "asc" → kasvav, kõik muu → kahanev.
    // Lisaks alati id järgi (view id = row_number üle training_id), et võrdsete väärtuste järjekord oleks stabiilne.
    static Sort createAdminTrainingSort(String sortBy, String sortDirection) {
        String sortProperty = ADMIN_TRAINING_SORT_PROPERTIES.getOrDefault(sortBy, DEFAULT_ADMIN_TRAINING_SORT_PROPERTY);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(new Sort.Order(direction, sortProperty), Sort.Order.asc("id"));
    }

    private static Specification<AdminTrainingSummary> createAdminTrainingSummarySpecification(AdminTrainingFilterDto adminTrainingFilterDto) {
        return Specification.allOf(
                AdminTrainingSummarySpecifications.hasContentLanguageCode(adminTrainingFilterDto.getContentLang()),
                AdminTrainingSummarySpecifications.hasStatus(adminTrainingFilterDto.getStatus()),
                AdminTrainingSummarySpecifications.hasCategoryId(adminTrainingFilterDto.getCategoryId()),
                AdminTrainingSummarySpecifications.hasTrainingLanguageId(adminTrainingFilterDto.getTrainingLanguageId()),
                AdminTrainingSummarySpecifications.hasFundingTypeId(adminTrainingFilterDto.getFundingTypeId()),
                AdminTrainingSummarySpecifications.hasIsOrderable(adminTrainingFilterDto.getIsOrderable()),
                AdminTrainingSummarySpecifications.hasIsPromoted(adminTrainingFilterDto.getIsPromoted()),
                AdminTrainingSummarySpecifications.hasAllTranslations(adminTrainingFilterDto.getHasAllTranslations()),
                AdminTrainingSummarySpecifications.titleContainsAllWords(adminTrainingFilterDto.getSearchText()));
    }

    private List<AdminTrainingSummaryItemDto> findAndCreateAdminTrainingSummaryItemDtos(String contentLang, Page<AdminTrainingSummary> adminTrainingSummaryPage) {
        List<AdminTrainingSummaryItemDto> adminTrainingSummaryItemDtos = adminTrainingSummaryMapper.toAdminTrainingSummaryItemDtos(adminTrainingSummaryPage.getContent());
        for (AdminTrainingSummaryItemDto adminTrainingSummaryItemDto : adminTrainingSummaryItemDtos) {
            handleAddFundingTypes(adminTrainingSummaryItemDto, contentLang);
        }
        return adminTrainingSummaryItemDtos;
    }

    private void handleAddFundingTypes(AdminTrainingSummaryItemDto adminTrainingSummaryItemDto, String contentLang) {
        List<FundingTypeTranslation> fundingTypeTranslations = fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(adminTrainingSummaryItemDto.getTrainingId(), contentLang);
        adminTrainingSummaryItemDto.setFundingTypes(fundingTypeTranslationMapper.toFundingTypeDtos(fundingTypeTranslations));
    }

    // Aktiivsete (status U, P) koolituste nimed contentLang keeles, puuduva tõlke korral põhikeeles
    public List<TrainingTitleDto> getTrainingTitles(String contentLang) {
        List<AdminTrainingSummary> adminTrainingSummaries = adminTrainingSummaryRepository
                .findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc(contentLang, TrainingStatus.DELETED.getCode());
        return adminTrainingSummaryMapper.toTrainingTitleDtos(adminTrainingSummaries);
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

    // null eemaldab lektori (muutmisel oluline — lisamisel on lektor niikuinii null)
    private void handleSetDefaultLecturer(Training training, Integer defaultLecturerId) {
        if (defaultLecturerId != null) {
            training.setDefaultLecturer(lecturerService.getValidLecturerBy(defaultLecturerId, "defaultLecturerId"));
        } else {
            training.setDefaultLecturer(null);
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
        trainingTranslation.setDescription(HtmlSanitizer.sanitizeDescription(trainingCreateRequestDto.getDescription()));
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(languageService.getMainLanguage());
        return trainingTranslation;
    }

    // Muudab koolituse väljad, rahastustüübid (üle kirjutades) ja avatud tõlke ühes transaktsioonis.
    // user, status ja created_at ei muutu; teiste keelte tõlkeid ei puudutata.
    @Transactional
    public void updateTraining(Integer trainingId, TrainingUpdateRequestDto trainingUpdateRequestDto) {
        Training training = getValidTrainingBy(trainingId);
        TrainingTranslation trainingTranslation = trainingTranslationService
                .getValidTrainingTranslationBy(trainingUpdateRequestDto.getTrainingTranslationId(), trainingId);
        updateTrainingData(training, trainingUpdateRequestDto);
        trainingRepository.save(training);
        replaceTrainingFundingTypes(training, trainingUpdateRequestDto.getFundingTypeIds());
        updateTrainingTranslationTexts(trainingTranslation, trainingUpdateRequestDto);
        trainingTranslationRepository.save(trainingTranslation);
    }

    private void updateTrainingData(Training training, TrainingUpdateRequestDto trainingUpdateRequestDto) {
        trainingMapper.updateTraining(trainingUpdateRequestDto, training);
        training.setCategory(categoryService.getValidCategoryBy(trainingUpdateRequestDto.getCategoryId()));
        training.setTrainingLanguage(languageService.getValidLanguageBy(trainingUpdateRequestDto.getTrainingLanguageId(), "trainingLanguageId"));
        training.setLocation(locationService.getValidLocationBy(trainingUpdateRequestDto.getLocationId()));
        handleSetDefaultLecturer(training, trainingUpdateRequestDto.getDefaultLecturerId());
    }

    private void replaceTrainingFundingTypes(Training training, List<Integer> fundingTypeIds) {
        trainingFundingTypeRepository.deleteTrainingFundingTypesBy(training.getId());
        addTrainingFundingTypes(training, fundingTypeIds);
    }

    private void updateTrainingTranslationTexts(TrainingTranslation trainingTranslation, TrainingUpdateRequestDto trainingUpdateRequestDto) {
        trainingTranslationMapper.updateTrainingTranslation(trainingUpdateRequestDto, trainingTranslation);
        trainingTranslation.setDescription(HtmlSanitizer.sanitizeDescription(trainingUpdateRequestDto.getDescription()));
    }

    // Lisab koolitusele tõlke uude keelde; koolituse rida ega staatust ei muudeta
    @Transactional
    public TrainingTranslationCreateResponseDto addTrainingTranslation(Integer trainingId, TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto) {
        Training training = getValidTrainingBy(trainingId);
        Language language = languageService.getValidLanguageBy(trainingTranslationCreateRequestDto.getLanguageId(), "languageId");
        validateTranslationDoesNotExist(trainingId, language.getId());
        TrainingTranslation trainingTranslation = createTrainingTranslation(training, language, trainingTranslationCreateRequestDto);
        trainingTranslationRepository.save(trainingTranslation);
        return new TrainingTranslationCreateResponseDto(trainingTranslation.getId());
    }

    // Kontroll enne salvestamist — muidu annaks training_translation_uq andmebaasi vea (500)
    private void validateTranslationDoesNotExist(Integer trainingId, Integer languageId) {
        if (trainingTranslationRepository.existsByTraining_IdAndLanguage_Id(trainingId, languageId)) {
            throw new ForbiddenException(TRANSLATION_EXISTS.getMessage(), TRANSLATION_EXISTS.name());
        }
    }

    private TrainingTranslation createTrainingTranslation(Training training, Language language, TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto) {
        TrainingTranslation trainingTranslation = trainingTranslationMapper.toTrainingTranslation(trainingTranslationCreateRequestDto);
        trainingTranslation.setDescription(HtmlSanitizer.sanitizeDescription(trainingTranslationCreateRequestDto.getDescription()));
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(language);
        return trainingTranslation;
    }

    // Soft delete: ainult status = D (updated_at uueneb auditeerimisega), tõlked ja rahastustüübid jäävad alles.
    // getValidTrainingBy leiab ka kustutatud koolituse — korduv kustutamine ei muuda midagi.
    @Transactional
    public void deleteTraining(Integer trainingId) {
        Training training = getValidTrainingBy(trainingId);
        if (TrainingStatus.DELETED.getCode().equals(training.getStatus())) {
            return;
        }
        training.setStatus(TrainingStatus.DELETED.getCode());
        trainingRepository.save(training);
    }

    // Taastab kustutatud koolituse mustandisse (D → U); avalikuks muutub see alles publitseerimisega.
    // Kustutamata koolituse (U, P) korral midagi ei muutu.
    @Transactional
    public void restoreTraining(Integer trainingId) {
        Training training = getValidTrainingBy(trainingId);
        if (!TrainingStatus.DELETED.getCode().equals(training.getStatus())) {
            return;
        }
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        trainingRepository.save(training);
    }

    @Transactional
    public void publishTraining(Integer trainingId) {
        changeTrainingStatus(trainingId, TrainingStatus.PUBLISHED);
    }

    @Transactional
    public void unpublishTraining(Integer trainingId) {
        changeTrainingStatus(trainingId, TrainingStatus.UNPUBLISHED);
    }

    // getValidTrainingBy leiab ka kustutatud koolituse, et anda 403 (mitte 404).
    // Juba soovitud staatuses koolituse korral midagi ei muutu (updated_at jääb samaks).
    private void changeTrainingStatus(Integer trainingId, TrainingStatus newTrainingStatus) {
        Training training = getValidTrainingBy(trainingId);
        validateTrainingIsNotDeleted(training);
        if (newTrainingStatus.getCode().equals(training.getStatus())) {
            return;
        }
        training.setStatus(newTrainingStatus.getCode());
        trainingRepository.save(training);
    }

    private static void validateTrainingIsNotDeleted(Training training) {
        if (TrainingStatus.DELETED.getCode().equals(training.getStatus())) {
            throw new ForbiddenException(TRAINING_DELETED.getMessage(), TRAINING_DELETED.name());
        }
    }
}
