package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.training.dto.TrainingTitleDto;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapper;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTitlesTest {

    @Mock
    private AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    @Spy
    private AdminTrainingSummaryMapper adminTrainingSummaryMapper = new AdminTrainingSummaryMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void getTrainingTitles_returnsActiveTrainingTitlesInRepositoryOrder() {
        when(adminTrainingSummaryRepository.findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc("et", "D"))
                .thenReturn(List.of(
                        createAdminTrainingSummary(7, "Agiilne meeskonnajuhtimine"),
                        createAdminTrainingSummary(10, "Docker ja konteinerid")));

        List<TrainingTitleDto> trainingTitleDtos = trainingService.getTrainingTitles("et");

        assertEquals(List.of(
                new TrainingTitleDto(7, "Agiilne meeskonnajuhtimine"),
                new TrainingTitleDto(10, "Docker ja konteinerid")), trainingTitleDtos);
    }

    @Test
    void getTrainingTitles_excludesDeletedTrainings() {
        trainingService.getTrainingTitles("en");

        verify(adminTrainingSummaryRepository)
                .findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc("en", TrainingStatus.DELETED.getCode());
    }

    @Test
    void getTrainingTitles_noTrainings_returnsEmptyList() {
        when(adminTrainingSummaryRepository.findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc("et", "D"))
                .thenReturn(List.of());

        assertTrue(trainingService.getTrainingTitles("et").isEmpty());
    }

    private static AdminTrainingSummary createAdminTrainingSummary(Integer trainingId, String title) {
        Training training = new Training();
        training.setId(trainingId);
        AdminTrainingSummary adminTrainingSummary = new AdminTrainingSummary();
        ReflectionTestUtils.setField(adminTrainingSummary, "training", training);
        ReflectionTestUtils.setField(adminTrainingSummary, "title", title);
        return adminTrainingSummary;
    }
}
