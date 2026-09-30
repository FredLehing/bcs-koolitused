package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateResponseDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapperImpl;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingServiceAddTranslationTest {

    private static final Integer TRAINING_ID = 3;
    private static final Integer LANGUAGE_ID = 2;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;
    @Mock
    private LanguageService languageService;
    @Spy
    private TrainingTranslationMapper trainingTranslationMapper = new TrainingTranslationMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    private Training training;
    private Language language;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(TRAINING_ID);
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());

        language = new Language();
        language.setId(LANGUAGE_ID);

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(languageService.getValidLanguageBy(LANGUAGE_ID, "languageId")).thenReturn(language);
        when(trainingTranslationRepository.existsByTraining_IdAndLanguage_Id(TRAINING_ID, LANGUAGE_ID)).thenReturn(false);
        // Andmebaas annaks salvestamisel ID
        doAnswer(invocation -> {
            TrainingTranslation trainingTranslation = invocation.getArgument(0);
            trainingTranslation.setId(6);
            return trainingTranslation;
        }).when(trainingTranslationRepository).save(any(TrainingTranslation.class));
    }

    @Test
    void addTrainingTranslation_createsTranslationWithSanitizedDescription() {
        TrainingTranslationCreateResponseDto trainingTranslationCreateResponseDto =
                trainingService.addTrainingTranslation(TRAINING_ID, createTrainingTranslationCreateRequestDto());

        assertEquals(6, trainingTranslationCreateResponseDto.getTrainingTranslationId());
        ArgumentCaptor<TrainingTranslation> trainingTranslationCaptor = ArgumentCaptor.forClass(TrainingTranslation.class);
        verify(trainingTranslationRepository).save(trainingTranslationCaptor.capture());
        TrainingTranslation savedTrainingTranslation = trainingTranslationCaptor.getValue();
        assertSame(training, savedTrainingTranslation.getTraining());
        assertSame(language, savedTrainingTranslation.getLanguage());
        assertEquals("Power BI for Advanced Users", savedTrainingTranslation.getTitle());
        assertEquals("Data models, DAX and interactive reports.", savedTrainingTranslation.getShortDescription());
        assertEquals("<p>The course builds a <strong>data model</strong>.</p>", savedTrainingTranslation.getDescription());
    }

    @Test
    void addTrainingTranslation_doesNotChangeTraining() {
        trainingService.addTrainingTranslation(TRAINING_ID, createTrainingTranslationCreateRequestDto());

        assertEquals(TrainingStatus.UNPUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTrainingTranslation_existingTranslationThrowsTranslationExists() {
        when(trainingTranslationRepository.existsByTraining_IdAndLanguage_Id(TRAINING_ID, LANGUAGE_ID)).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainingService.addTrainingTranslation(TRAINING_ID, createTrainingTranslationCreateRequestDto()));

        assertEquals("Selles keeles tõlge on juba olemas", exception.getMessage());
        assertEquals("TRANSLATION_EXISTS", exception.getErrorCode());
        verify(trainingTranslationRepository, never()).save(any());
    }

    @Test
    void addTrainingTranslation_unknownTrainingIdThrows() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.addTrainingTranslation(123, createTrainingTranslationCreateRequestDto()));

        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 123", exception.getMessage());
        verify(trainingTranslationRepository, never()).save(any());
    }

    @Test
    void addTrainingTranslation_unknownLanguageIdThrows() {
        TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto = createTrainingTranslationCreateRequestDto();
        trainingTranslationCreateRequestDto.setLanguageId(123);
        when(languageService.getValidLanguageBy(123, "languageId"))
                .thenThrow(new PrimaryKeyNotFoundException("languageId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.addTrainingTranslation(TRAINING_ID, trainingTranslationCreateRequestDto));

        assertEquals("Ei leidnud primary keyd 'languageId' väärtusega: 123", exception.getMessage());
        verify(trainingTranslationRepository, never()).save(any());
    }

    private TrainingTranslationCreateRequestDto createTrainingTranslationCreateRequestDto() {
        return new TrainingTranslationCreateRequestDto(
                LANGUAGE_ID, "Power BI for Advanced Users", "Data models, DAX and interactive reports.",
                "<p>The course builds a <strong>data model</strong>.</p><img src=x onerror=\"alert(1)\">");
    }
}
