package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.trainingtranslation.dto.TrainingTranslationDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapperImpl;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingTranslationServiceTest {

    private static final String DESCRIPTION_HTML =
            "<p>Kursusel õpitakse <strong>Java süntaksit</strong>.</p><ul><li><p>Klassid</p></li></ul>";

    private TrainingTranslationRepository trainingTranslationRepository;
    private TrainingTranslationCurriculumService trainingTranslationCurriculumService;
    private TrainingTranslationService trainingTranslationService;

    @BeforeEach
    void setUp() {
        trainingTranslationRepository = mock(TrainingTranslationRepository.class);
        trainingTranslationCurriculumService = mock(TrainingTranslationCurriculumService.class);
        when(trainingTranslationCurriculumService.findCurriculumInfo(1)).thenReturn(Optional.empty());
        trainingTranslationService = new TrainingTranslationService(
                trainingTranslationRepository, new TrainingTranslationMapperImpl(), trainingTranslationCurriculumService);
    }

    @Test
    void getTrainingTranslation_returnsTranslationWithLanguageCodeAndUnchangedHtml() {
        when(trainingTranslationRepository.findById(1)).thenReturn(Optional.of(createTrainingTranslation()));

        TrainingTranslationDto trainingTranslationDto = trainingTranslationService.getTrainingTranslation(1);

        assertEquals(1, trainingTranslationDto.getTrainingTranslationId());
        assertEquals(3, trainingTranslationDto.getTrainingId());
        assertEquals(2, trainingTranslationDto.getLanguageId());
        assertEquals("en", trainingTranslationDto.getLanguageCode());
        assertEquals("Java Basics", trainingTranslationDto.getTitle());
        assertEquals("Fundamentals of Java.", trainingTranslationDto.getShortDescription());
        assertEquals(DESCRIPTION_HTML, trainingTranslationDto.getDescription());
        assertNull(trainingTranslationDto.getCurriculumFileName());
        assertNull(trainingTranslationDto.getCurriculumFileSize());
    }

    @Test
    void getTrainingTranslation_returnsCurriculumFileNameAndSize() {
        when(trainingTranslationRepository.findById(1)).thenReturn(Optional.of(createTrainingTranslation()));
        when(trainingTranslationCurriculumService.findCurriculumInfo(1)).thenReturn(Optional.of(new TrainingTranslationCurriculumInfo() {
            @Override
            public String getFileName() {
                return "java-basics-curriculum.pdf";
            }

            @Override
            public Integer getFileSize() {
                return 846213;
            }
        }));

        TrainingTranslationDto trainingTranslationDto = trainingTranslationService.getTrainingTranslation(1);

        assertEquals("java-basics-curriculum.pdf", trainingTranslationDto.getCurriculumFileName());
        assertEquals(846213, trainingTranslationDto.getCurriculumFileSize());
    }

    @Test
    void getTrainingTranslation_throwsPrimaryKeyNotFoundForUnknownId() {
        when(trainingTranslationRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingTranslationService.getTrainingTranslation(123));

        assertEquals("Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123", exception.getMessage());
        assertEquals("PRIMARY_KEY_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getTrainingTranslation_deletedTraining_throwsPrimaryKeyNotFound() {
        TrainingTranslation trainingTranslation = createTrainingTranslation();
        trainingTranslation.getTraining().setStatus(TrainingStatus.DELETED.getCode());
        when(trainingTranslationRepository.findById(1)).thenReturn(Optional.of(trainingTranslation));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingTranslationService.getTrainingTranslation(1));

        assertEquals("Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 1", exception.getMessage());
    }

    private TrainingTranslation createTrainingTranslation() {
        Training training = new Training();
        training.setId(3);

        Language language = new Language();
        language.setId(2);
        language.setCode("en");

        TrainingTranslation trainingTranslation = new TrainingTranslation();
        trainingTranslation.setId(1);
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(language);
        trainingTranslation.setTitle("Java Basics");
        trainingTranslation.setShortDescription("Fundamentals of Java.");
        trainingTranslation.setDescription(DESCRIPTION_HTML);
        return trainingTranslation;
    }
}
