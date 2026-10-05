package ee.bcskoolitus.controller.trainingtranslation;

import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculum;
import ee.bcskoolitus.service.TrainingTranslationCurriculumService;
import ee.bcskoolitus.service.TrainingTranslationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// GET /api/training-translation/{trainingTranslationId}/curriculum — päised ja 404
@ExtendWith(MockitoExtension.class)
class TrainingTranslationControllerCurriculumTest {

    private static final byte[] PDF_BYTES = "%PDF-1.7\n%test".getBytes(StandardCharsets.US_ASCII);

    @Mock
    private TrainingTranslationService trainingTranslationService;
    @Mock
    private TrainingTranslationCurriculumService trainingTranslationCurriculumService;

    @InjectMocks
    private TrainingTranslationController trainingTranslationController;

    @Test
    void getTrainingTranslationCurriculum_returnsPdfAsAttachmentWithStoredFileName() {
        TrainingTranslationCurriculum trainingTranslationCurriculum = new TrainingTranslationCurriculum();
        trainingTranslationCurriculum.setFile(PDF_BYTES);
        trainingTranslationCurriculum.setFileName("java-algkursus-oppekava.pdf");
        when(trainingTranslationCurriculumService.getValidTrainingTranslationCurriculumBy(1)).thenReturn(trainingTranslationCurriculum);

        ResponseEntity<byte[]> response = trainingTranslationController.getTrainingTranslationCurriculum(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"java-algkursus-oppekava.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("no-cache", response.getHeaders().getCacheControl());
        assertArrayEquals(PDF_BYTES, response.getBody());
    }

    @Test
    void getTrainingTranslationCurriculum_deletedTrainingThrowsPrimaryKeyNotFound() {
        when(trainingTranslationService.getValidActiveTrainingTranslationBy(1))
                .thenThrow(new PrimaryKeyNotFoundException("trainingTranslationId", 1));

        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingTranslationController.getTrainingTranslationCurriculum(1));
        verifyNoInteractions(trainingTranslationCurriculumService);
    }

    @Test
    void getTrainingTranslationCurriculum_withoutCurriculumThrowsPrimaryKeyNotFound() {
        when(trainingTranslationCurriculumService.getValidTrainingTranslationCurriculumBy(1))
                .thenThrow(new PrimaryKeyNotFoundException("trainingTranslationId", 1));

        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingTranslationController.getTrainingTranslationCurriculum(1));
    }
}
