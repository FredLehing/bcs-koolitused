package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Kustutatud koolitus (status D) on teenustele nagu olematu → 404 PRIMARY_KEY_NOT_FOUND
@ExtendWith(MockitoExtension.class)
class TrainingServiceActiveTrainingTest {

    private static final Integer TRAINING_ID = 14;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;

    @InjectMocks
    private TrainingService trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(TRAINING_ID);
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
    }

    @Test
    void getValidActiveTrainingBy_unpublishedOrPublishedTraining_returnsTraining() {
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        assertSame(training, trainingService.getValidActiveTrainingBy(TRAINING_ID));

        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        assertSame(training, trainingService.getValidActiveTrainingBy(TRAINING_ID));
    }

    @Test
    void getValidActiveTrainingBy_deletedTraining_throwsPrimaryKeyNotFound() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertTrainingNotFound(() -> trainingService.getValidActiveTrainingBy(TRAINING_ID));
    }

    @Test
    void getTraining_deletedTraining_throwsPrimaryKeyNotFound() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertTrainingNotFound(() -> trainingService.getTraining(TRAINING_ID));
    }

    @Test
    void getTrainingTranslations_deletedTraining_throwsPrimaryKeyNotFound() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertTrainingNotFound(() -> trainingService.getTrainingTranslations(TRAINING_ID));
        verifyNoInteractions(trainingTranslationRepository);
    }

    @Test
    void updateTraining_deletedTraining_throwsPrimaryKeyNotFoundAndSavesNothing() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertTrainingNotFound(() -> trainingService.updateTraining(TRAINING_ID, new TrainingUpdateRequestDto()));
        verify(trainingRepository, never()).save(any());
        verifyNoInteractions(trainingTranslationRepository);
    }

    @Test
    void addTrainingTranslation_deletedTraining_throwsPrimaryKeyNotFoundAndSavesNothing() {
        training.setStatus(TrainingStatus.DELETED.getCode());

        assertTrainingNotFound(() -> trainingService.addTrainingTranslation(TRAINING_ID, new TrainingTranslationCreateRequestDto()));
        verifyNoInteractions(trainingTranslationRepository);
    }

    private static void assertTrainingNotFound(Executable executable) {
        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, executable);
        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 14", exception.getMessage());
        assertEquals("PRIMARY_KEY_NOT_FOUND", exception.getErrorCode());
    }
}
