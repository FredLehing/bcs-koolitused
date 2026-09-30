package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapperImpl;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.location.Location;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturer;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerMapper;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerMapperImpl;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// GET /api/admin-training/{trainingId}: tekstid contentLang keeles, puudumisel põhikeeles; koolitajad järjekorras
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingServiceAdminTrainingTest {

    private static final Integer TRAINING_ID = 1;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    @Mock
    private TrainingTranslationService trainingTranslationService;
    @Mock
    private TrainingLecturerRepository trainingLecturerRepository;
    @Mock
    private FundingTypeTranslationRepository fundingTypeTranslationRepository;
    @Mock
    private LanguageService languageService;
    @Spy
    private AdminTrainingSummaryMapper adminTrainingSummaryMapper = new AdminTrainingSummaryMapperImpl();
    @Spy
    private TrainingLecturerMapper trainingLecturerMapper = new TrainingLecturerMapperImpl();
    @Spy
    private FundingTypeTranslationMapper fundingTypeTranslationMapper = new FundingTypeTranslationMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        Location location = new Location();
        location.setName("BCS Koolitus");
        training = new Training();
        training.setId(TRAINING_ID);
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        training.setLocation(location);
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        Language mainLanguage = new Language();
        mainLanguage.setCode("et");
        when(languageService.getMainLanguage()).thenReturn(mainLanguage);
        TrainingTranslation trainingTranslation = new TrainingTranslation();
        trainingTranslation.setDescription("<p>Java</p>");
        when(trainingTranslationService.getValidTrainingTranslationBy(1)).thenReturn(trainingTranslation);
        TrainingLecturer trainingLecturer = new TrainingLecturer();
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFullName("Rain Tüür");
        trainingLecturer.setLecturer(lecturer);
        when(trainingLecturerRepository.findTrainingLecturersBy(TRAINING_ID)).thenReturn(List.of(trainingLecturer));
        when(fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(TRAINING_ID, "ru")).thenReturn(List.of());
    }

    @Test
    void getAdminTraining_missingContentLangFallsBackToMainLanguage() {
        when(adminTrainingSummaryRepository.findByTraining_IdAndContentLanguageCode(TRAINING_ID, "ru")).thenReturn(Optional.empty());
        when(adminTrainingSummaryRepository.findByTraining_IdAndContentLanguageCode(TRAINING_ID, "et"))
                .thenReturn(Optional.of(createAdminTrainingSummary("Java algkursus")));

        AdminTrainingDto adminTrainingDto = trainingService.getAdminTraining(TRAINING_ID, "ru");

        assertEquals("Java algkursus", adminTrainingDto.getTitle());
        assertEquals(1, adminTrainingDto.getTrainingTranslationId());
        assertEquals("<p>Java</p>", adminTrainingDto.getDescription());
        assertEquals("BCS Koolitus", adminTrainingDto.getLocationName());
        assertEquals("U", adminTrainingDto.getStatus());
        assertEquals(List.of(new LecturerDto(1, "Rain Tüür")), adminTrainingDto.getLecturers());
        assertEquals(List.of(), adminTrainingDto.getFundingTypes());
    }

    @Test
    void getAdminTraining_deletedTrainingThrows() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingService.getAdminTraining(TRAINING_ID, "et"));
    }

    private AdminTrainingSummary createAdminTrainingSummary(String title) {
        AdminTrainingSummary adminTrainingSummary = new AdminTrainingSummary();
        ReflectionTestUtils.setField(adminTrainingSummary, "training", training);
        ReflectionTestUtils.setField(adminTrainingSummary, "trainingTranslationId", 1);
        ReflectionTestUtils.setField(adminTrainingSummary, "title", title);
        ReflectionTestUtils.setField(adminTrainingSummary, "status", "U");
        return adminTrainingSummary;
    }
}
