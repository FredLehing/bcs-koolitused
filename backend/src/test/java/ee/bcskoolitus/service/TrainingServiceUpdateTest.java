package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.location.Location;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingMapper;
import ee.bcskoolitus.persistance.training.TrainingMapperImpl;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingTypeRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapperImpl;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingServiceUpdateTest {

    private static final Integer TRAINING_ID = 1;
    private static final Integer TRAINING_TRANSLATION_ID = 2;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;
    @Mock
    private TrainingFundingTypeRepository trainingFundingTypeRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private LanguageService languageService;
    @Mock
    private LocationService locationService;
    @Mock
    private LecturerService lecturerService;
    @Mock
    private FundingTypeService fundingTypeService;
    @Mock
    private TrainingTranslationService trainingTranslationService;
    @Spy
    private TrainingMapper trainingMapper = new TrainingMapperImpl();
    @Spy
    private TrainingTranslationMapper trainingTranslationMapper = new TrainingTranslationMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    private User user;
    private Language translationLanguage;
    private Training training;
    private TrainingTranslation trainingTranslation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        translationLanguage = new Language();
        translationLanguage.setId(2);

        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);

        training = new Training();
        training.setId(TRAINING_ID);
        training.setUser(user);
        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        training.setDefaultLecturer(lecturer);
        training.setIsOrderable(true);
        training.setIsPromoted(true);

        trainingTranslation = new TrainingTranslation();
        trainingTranslation.setId(TRAINING_TRANSLATION_ID);
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(translationLanguage);
        trainingTranslation.setTitle("Java Basics");

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingTranslationService.getValidTrainingTranslationBy(TRAINING_TRANSLATION_ID, TRAINING_ID))
                .thenReturn(trainingTranslation);
        when(categoryService.getValidCategoryBy(3)).thenReturn(createCategory(3));
        when(languageService.getValidLanguageBy(1, "trainingLanguageId")).thenReturn(createLanguage(1));
        when(locationService.getValidLocationBy(2)).thenReturn(createLocation(2));
        when(fundingTypeService.getValidFundingTypeBy(1)).thenReturn(createFundingType(1));
        when(fundingTypeService.getValidFundingTypeBy(2)).thenReturn(createFundingType(2));
    }

    @Test
    void updateTraining_updatesTrainingAndKeepsUserAndStatus() {
        trainingService.updateTraining(TRAINING_ID, createTrainingUpdateRequestDto());

        assertEquals(3, training.getCategory().getId());
        assertEquals(1, training.getTrainingLanguage().getId());
        assertEquals(2, training.getLocation().getId());
        assertFalse(training.getIsOrderable());
        assertFalse(training.getIsPromoted());
        assertSame(user, training.getUser());
        assertEquals(TrainingStatus.PUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void updateTraining_nullDefaultLecturerRemovesLecturer() {
        trainingService.updateTraining(TRAINING_ID, createTrainingUpdateRequestDto());

        assertNull(training.getDefaultLecturer());
        verifyNoInteractions(lecturerService);
    }

    @Test
    void updateTraining_updatesOpenTranslationWithSanitizedDescription() {
        trainingService.updateTraining(TRAINING_ID, createTrainingUpdateRequestDto());

        assertEquals("Java Advanced", trainingTranslation.getTitle());
        assertEquals("Advanced Java topics.", trainingTranslation.getShortDescription());
        assertEquals("<p>Streams and <strong>lambdas</strong></p>", trainingTranslation.getDescription());
        assertSame(translationLanguage, trainingTranslation.getLanguage());
        assertSame(training, trainingTranslation.getTraining());
        verify(trainingTranslationRepository).save(trainingTranslation);
    }

    @Test
    void updateTraining_replacesFundingTypesWithoutDuplicates() {
        trainingService.updateTraining(TRAINING_ID, createTrainingUpdateRequestDto());

        InOrder inOrder = inOrder(trainingFundingTypeRepository);
        inOrder.verify(trainingFundingTypeRepository).deleteTrainingFundingTypesBy(TRAINING_ID);
        ArgumentCaptor<TrainingFundingType> trainingFundingTypeCaptor = ArgumentCaptor.forClass(TrainingFundingType.class);
        inOrder.verify(trainingFundingTypeRepository, times(2)).save(trainingFundingTypeCaptor.capture());
        List<Integer> savedFundingTypeIds = trainingFundingTypeCaptor.getAllValues().stream()
                .map(trainingFundingType -> trainingFundingType.getFundingType().getId())
                .toList();
        assertEquals(List.of(1, 2), savedFundingTypeIds);
    }

    @Test
    void updateTraining_emptyFundingTypeIdsRemovesAllFundingTypes() {
        TrainingUpdateRequestDto trainingUpdateRequestDto = createTrainingUpdateRequestDto();
        trainingUpdateRequestDto.setFundingTypeIds(List.of());

        trainingService.updateTraining(TRAINING_ID, trainingUpdateRequestDto);

        verify(trainingFundingTypeRepository).deleteTrainingFundingTypesBy(TRAINING_ID);
        verify(trainingFundingTypeRepository, never()).save(any());
    }

    @Test
    void updateTraining_unknownTrainingIdThrowsAndSavesNothing() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.updateTraining(123, createTrainingUpdateRequestDto()));

        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 123", exception.getMessage());
        verify(trainingRepository, never()).save(any());
        verifyNoInteractions(trainingFundingTypeRepository, trainingTranslationRepository);
    }

    @Test
    void updateTraining_translationOfAnotherTrainingThrowsAndSavesNothing() {
        TrainingUpdateRequestDto trainingUpdateRequestDto = createTrainingUpdateRequestDto();
        trainingUpdateRequestDto.setTrainingTranslationId(3);
        when(trainingTranslationService.getValidTrainingTranslationBy(3, TRAINING_ID))
                .thenThrow(new PrimaryKeyNotFoundException("trainingTranslationId", 3));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.updateTraining(TRAINING_ID, trainingUpdateRequestDto));

        assertEquals("Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 3", exception.getMessage());
        verify(trainingRepository, never()).save(any());
        verifyNoInteractions(trainingFundingTypeRepository, trainingTranslationRepository);
    }

    private TrainingUpdateRequestDto createTrainingUpdateRequestDto() {
        return new TrainingUpdateRequestDto(
                3, 1, 2, null, false, false, List.of(1, 2, 1),
                TRAINING_TRANSLATION_ID, "Java Advanced", "Advanced Java topics.",
                "<p onclick=\"alert(1)\">Streams and <strong>lambdas</strong></p><script>alert(1)</script>");
    }

    private static Category createCategory(Integer categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }

    private static Language createLanguage(Integer languageId) {
        Language language = new Language();
        language.setId(languageId);
        return language;
    }

    private static Location createLocation(Integer locationId) {
        Location location = new Location();
        location.setId(locationId);
        return location;
    }

    private static FundingType createFundingType(Integer fundingTypeId) {
        FundingType fundingType = new FundingType();
        fundingType.setId(fundingTypeId);
        return fundingType;
    }
}
