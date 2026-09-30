package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingTypeRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceDeleteTest {

    private static final Integer TRAINING_ID = 1;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;
    @Mock
    private TrainingFundingTypeRepository trainingFundingTypeRepository;

    @InjectMocks
    private TrainingService trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(TRAINING_ID);
    }

    @Test
    void deleteTraining_setsPublishedTrainingStatusToDeleted() {
        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(TRAINING_ID);

        assertEquals(TrainingStatus.DELETED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void deleteTraining_setsUnpublishedTrainingStatusToDeleted() {
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(TRAINING_ID);

        assertEquals(TrainingStatus.DELETED.getCode(), training.getStatus());
        verify(trainingRepository).save(training);
    }

    @Test
    void deleteTraining_keepsTranslationsAndFundingTypes() {
        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(TRAINING_ID);

        verifyNoInteractions(trainingTranslationRepository, trainingFundingTypeRepository);
    }

    @Test
    void deleteTraining_alreadyDeletedTraining_changesNothing() {
        training.setStatus(TrainingStatus.DELETED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(TRAINING_ID);

        assertEquals(TrainingStatus.DELETED.getCode(), training.getStatus());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void deleteTraining_unknownTrainingId_throwsPrimaryKeyNotFound() {
        when(trainingRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.deleteTraining(123));

        assertEquals("Ei leidnud primary keyd 'trainingId' väärtusega: 123", exception.getMessage());
        verify(trainingRepository, never()).save(any());
    }
}
