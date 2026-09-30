package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingFilterDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryItemDto;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslation;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapperImpl;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceAdminTrainingsTest {

    @Mock
    private AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    @Mock
    private FundingTypeTranslationRepository fundingTypeTranslationRepository;
    @Spy
    private AdminTrainingSummaryMapper adminTrainingSummaryMapper = new AdminTrainingSummaryMapperImpl();
    @Spy
    private FundingTypeTranslationMapper fundingTypeTranslationMapper = new FundingTypeTranslationMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    // ---------- sorteerimine ----------

    @Test
    void createAdminTrainingSort_mapsSortByToViewProperty() {
        assertEquals(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("createdAt", "asc"));
        assertEquals(Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("updatedAt", "desc"));
        assertEquals(Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("title", "asc"));
        assertEquals(Sort.by(Sort.Order.asc("categoryName"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("categoryName", "asc"));
        assertEquals(Sort.by(Sort.Order.asc("trainingLanguageCode"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("trainingLanguageCode", "asc"));
        assertEquals(Sort.by(Sort.Order.asc("hasAllTranslations"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("hasAllTranslations", "asc"));
    }

    @Test
    void createAdminTrainingSort_statusUsesWorkflowOrder() {
        assertEquals(Sort.by(Sort.Order.asc("statusOrder"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("status", "asc"));
    }

    @Test
    void createAdminTrainingSort_unknownSortByFallsBackToCreatedAt() {
        assertEquals(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("shortDescription", "desc"));
    }

    @Test
    void createAdminTrainingSort_directionOtherThanAscIsDesc() {
        assertEquals(Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("title", "ASC"));
        assertEquals(Sort.by(Sort.Order.desc("title"), Sort.Order.asc("id")), TrainingService.createAdminTrainingSort("title", "xyz"));
    }

    // ---------- päring ja vastus ----------

    @Test
    void findAdminTrainings_usesPageLimitAndSort() {
        when(adminTrainingSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        trainingService.findAdminTrainings(createAdminTrainingFilterDto());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminTrainingSummaryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(PageRequest.of(1, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))), pageableCaptor.getValue());
    }

    @Test
    void findAdminTrainings_mapsRowsAndPageMetadata() {
        AdminTrainingSummary adminTrainingSummary = createAdminTrainingSummary();
        Page<AdminTrainingSummary> adminTrainingSummaryPage = new PageImpl<>(List.of(adminTrainingSummary), PageRequest.of(0, 1), 13);
        when(adminTrainingSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adminTrainingSummaryPage);
        when(fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(13, "et"))
                .thenReturn(List.of(createFundingTypeTranslation(1, "Töötukassa"), createFundingTypeTranslation(2, "EL rahastus")));

        AdminTrainingSummaryDto adminTrainingSummaryDto = trainingService.findAdminTrainings(createAdminTrainingFilterDto());

        assertEquals(13, adminTrainingSummaryDto.getTotalPages());
        assertEquals(13L, adminTrainingSummaryDto.getTotalElements());
        AdminTrainingSummaryItemDto adminTrainingSummaryItemDto = adminTrainingSummaryDto.getAdminTrainingSummaries().getFirst();
        assertEquals(13, adminTrainingSummaryItemDto.getTrainingId());
        assertEquals(23, adminTrainingSummaryItemDto.getTrainingTranslationId());
        assertEquals("Tehisaru töövahendid arendajale", adminTrainingSummaryItemDto.getTitle());
        assertEquals("Programmeerimine", adminTrainingSummaryItemDto.getCategoryName());
        assertEquals("fi-ee", adminTrainingSummaryItemDto.getTrainingLanguageFlagIconCode());
        assertEquals("P", adminTrainingSummaryItemDto.getStatus());
        assertEquals(Instant.parse("2026-09-25T12:10:00Z"), adminTrainingSummaryItemDto.getCreatedAt());
        assertEquals(Instant.parse("2026-09-26T06:00:00Z"), adminTrainingSummaryItemDto.getUpdatedAt());
        assertEquals(false, adminTrainingSummaryItemDto.getHasAllTranslations());
        assertEquals(List.of("en"), adminTrainingSummaryItemDto.getMissingTranslationLanguageCodes());
        assertEquals(List.of(new FundingTypeDto(1, "Töötukassa"), new FundingTypeDto(2, "EL rahastus")), adminTrainingSummaryItemDto.getFundingTypes());
    }

    @Test
    void findAdminTrainings_allTranslationsPresent_missingCodesIsEmptyList() {
        AdminTrainingSummary adminTrainingSummary = createAdminTrainingSummary();
        ReflectionTestUtils.setField(adminTrainingSummary, "missingTranslationLanguageCodes", null);
        ReflectionTestUtils.setField(adminTrainingSummary, "hasAllTranslations", true);
        when(adminTrainingSummaryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(adminTrainingSummary)));

        AdminTrainingSummaryDto adminTrainingSummaryDto = trainingService.findAdminTrainings(createAdminTrainingFilterDto());

        AdminTrainingSummaryItemDto adminTrainingSummaryItemDto = adminTrainingSummaryDto.getAdminTrainingSummaries().getFirst();
        assertTrue(adminTrainingSummaryItemDto.getMissingTranslationLanguageCodes().isEmpty());
        assertTrue(adminTrainingSummaryItemDto.getFundingTypes().isEmpty());
    }

    @Test
    void findAdminTrainings_noResults_returnsEmptyListAndZeroTotals() {
        when(adminTrainingSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        AdminTrainingSummaryDto adminTrainingSummaryDto = trainingService.findAdminTrainings(createAdminTrainingFilterDto());

        assertEquals(0L, adminTrainingSummaryDto.getTotalElements());
        assertTrue(adminTrainingSummaryDto.getAdminTrainingSummaries().isEmpty());
    }

    private static AdminTrainingFilterDto createAdminTrainingFilterDto() {
        AdminTrainingFilterDto adminTrainingFilterDto = new AdminTrainingFilterDto();
        adminTrainingFilterDto.setContentLang("et");
        adminTrainingFilterDto.setSearchText("");
        adminTrainingFilterDto.setCategoryId(0);
        adminTrainingFilterDto.setTrainingLanguageId(0);
        adminTrainingFilterDto.setFundingTypeId(0);
        adminTrainingFilterDto.setSortBy("createdAt");
        adminTrainingFilterDto.setSortDirection("desc");
        adminTrainingFilterDto.setPage(1);
        adminTrainingFilterDto.setLimit(10);
        return adminTrainingFilterDto;
    }

    // 3_import.sql koolitus 13 (et, publitseeritud, en tõlge puudub)
    private static AdminTrainingSummary createAdminTrainingSummary() {
        Training training = new Training();
        training.setId(13);
        AdminTrainingSummary adminTrainingSummary = new AdminTrainingSummary();
        ReflectionTestUtils.setField(adminTrainingSummary, "training", training);
        ReflectionTestUtils.setField(adminTrainingSummary, "trainingTranslationId", 23);
        ReflectionTestUtils.setField(adminTrainingSummary, "title", "Tehisaru töövahendid arendajale");
        ReflectionTestUtils.setField(adminTrainingSummary, "categoryId", 1);
        ReflectionTestUtils.setField(adminTrainingSummary, "categoryName", "Programmeerimine");
        ReflectionTestUtils.setField(adminTrainingSummary, "trainingLanguageCode", "et");
        ReflectionTestUtils.setField(adminTrainingSummary, "trainingLanguageFlagIconCode", "fi-ee");
        ReflectionTestUtils.setField(adminTrainingSummary, "status", "P");
        ReflectionTestUtils.setField(adminTrainingSummary, "isOrderable", true);
        ReflectionTestUtils.setField(adminTrainingSummary, "isPromoted", true);
        ReflectionTestUtils.setField(adminTrainingSummary, "createdAt", Instant.parse("2026-09-25T12:10:00Z"));
        ReflectionTestUtils.setField(adminTrainingSummary, "updatedAt", Instant.parse("2026-09-26T06:00:00Z"));
        ReflectionTestUtils.setField(adminTrainingSummary, "hasAllTranslations", false);
        ReflectionTestUtils.setField(adminTrainingSummary, "missingTranslationLanguageCodes", "en");
        return adminTrainingSummary;
    }

    private static FundingTypeTranslation createFundingTypeTranslation(Integer fundingTypeId, String name) {
        FundingType fundingType = new FundingType();
        fundingType.setId(fundingTypeId);
        FundingTypeTranslation fundingTypeTranslation = new FundingTypeTranslation();
        fundingTypeTranslation.setFundingType(fundingType);
        fundingTypeTranslation.setName(name);
        return fundingTypeTranslation;
    }
}
