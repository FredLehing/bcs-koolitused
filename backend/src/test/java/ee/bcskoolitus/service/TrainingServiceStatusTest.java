package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceStatusTest {

    private static final Integer TRAINING_ID = 10;

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(TRAINING_ID);
    }

    // ---------- publish ----------

    @Test
    void publishTraining_unpublishedTraining_becomesPublished() {
        givenTrainingWithStatus(TrainingStatus.UNPUBLISHED);

        trainingService.publishTraining(TRAINING_ID);

        assertEquals(TrainingStatus.PUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void publishTraining_publishedTraining_changesNothing() {
        givenTrainingWithStatus(TrainingStatus.PUBLISHED);

        trainingService.publishTraining(TRAINING_ID);

        assertEquals(TrainingStatus.PUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void publishTraining_deletedTraining_throwsTrainingDeleted() {
        givenTrainingWithStatus(TrainingStatus.DELETED);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainingService.publishTraining(TRAINING_ID));

        assertEquals("TRAINING_DELETED", exception.getErrorCode());
        assertEquals("Kustutatud koolituse staatust ei saa muuta, taasta see enne", exception.getMessage());
        assertEquals(TrainingStatus.DELETED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void publishTraining_unknownTrainingId_throwsPrimaryKeyNotFound() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.publishTraining(123));

        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 123", exception.getMessage());
    }

    // ---------- unpublish ----------

    @Test
    void unpublishTraining_publishedTraining_becomesUnpublished() {
        givenTrainingWithStatus(TrainingStatus.PUBLISHED);

        trainingService.unpublishTraining(TRAINING_ID);

        assertEquals(TrainingStatus.UNPUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void unpublishTraining_unpublishedTraining_changesNothing() {
        givenTrainingWithStatus(TrainingStatus.UNPUBLISHED);

        trainingService.unpublishTraining(TRAINING_ID);

        assertEquals(TrainingStatus.UNPUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void unpublishTraining_deletedTraining_throwsTrainingDeleted() {
        givenTrainingWithStatus(TrainingStatus.DELETED);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainingService.unpublishTraining(TRAINING_ID));

        assertEquals("TRAINING_DELETED", exception.getErrorCode());
        assertEquals(TrainingStatus.DELETED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void unpublishTraining_unknownTrainingId_throwsPrimaryKeyNotFound() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingService.unpublishTraining(123));
    }

    private void givenTrainingWithStatus(TrainingStatus trainingStatus) {
        training.setStatus(trainingStatus.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
    }
}
