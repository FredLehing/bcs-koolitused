package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
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
class TrainingServiceRestoreTest {

    private static final Integer TRAINING_ID = 14;

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

    @Test
    void restoreTraining_deletedTraining_becomesUnpublished() {
        training.setStatus(TrainingStatus.DELETED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.restoreTraining(TRAINING_ID);

        assertEquals(TrainingStatus.UNPUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void restoreTraining_publishedTraining_changesNothing() {
        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.restoreTraining(TRAINING_ID);

        assertEquals(TrainingStatus.PUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void restoreTraining_unpublishedTraining_changesNothing() {
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.restoreTraining(TRAINING_ID);

        assertEquals(TrainingStatus.UNPUBLISHED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void restoreTraining_unknownTrainingId_throwsPrimaryKeyNotFound() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.restoreTraining(123));

        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 123", exception.getMessage());
        verify(trainingRepository, never()).save(any());
    }
}
