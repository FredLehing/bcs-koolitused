package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculum;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumInfo;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingTranslationCurriculumServiceTest {

    private static final Integer TRAINING_ID = 1;
    private static final Integer TRAINING_TRANSLATION_ID = 1;
    private static final byte[] PDF_BYTES = "%PDF-1.7\n%test".getBytes(StandardCharsets.US_ASCII);
    private static final String PDF_BASE64 = Base64.getEncoder().encodeToString(PDF_BYTES);

    @Mock
    private TrainingTranslationCurriculumRepository trainingTranslationCurriculumRepository;

    @InjectMocks
    private TrainingTranslationCurriculumService trainingTranslationCurriculumService;

    private TrainingTranslation trainingTranslation;

    @BeforeEach
    void setUp() {
        Training training = new Training();
        training.setId(TRAINING_ID);
        trainingTranslation = new TrainingTranslation();
        trainingTranslation.setId(TRAINING_TRANSLATION_ID);
        trainingTranslation.setTraining(training);
        trainingTranslation.setTitle("Java algkursus");
        when(trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumBy(TRAINING_TRANSLATION_ID))
                .thenReturn(Optional.empty());
        when(trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumInfoBy(TRAINING_TRANSLATION_ID))
                .thenReturn(Optional.empty());
    }

    @Test
    void getValidTrainingTranslationCurriculumBy_withoutCurriculumThrowsPrimaryKeyNotFound() {
        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingTranslationCurriculumService.getValidTrainingTranslationCurriculumBy(TRAINING_TRANSLATION_ID));

        assertEquals("Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 1", exception.getMessage());
    }

    @Test
    void handleAddCurriculum_savesFileWithNameAndSize() {
        trainingTranslationCurriculumService.handleAddCurriculum(trainingTranslation, PDF_BASE64, "Õppekava");

        TrainingTranslationCurriculum savedCurriculum = captureSavedCurriculum();
        assertSame(trainingTranslation, savedCurriculum.getTrainingTranslation());
        assertArrayEquals(PDF_BYTES, savedCurriculum.getFile());
        assertEquals(PDF_BYTES.length, savedCurriculum.getFileSize());
        assertEquals("java-algkursus-oppekava.pdf", savedCurriculum.getFileName());
    }

    @Test
    void handleAddCurriculum_withoutFileDoesNothing() {
        trainingTranslationCurriculumService.handleAddCurriculum(trainingTranslation, null, "Õppekava");

        verifyNoInteractions(trainingTranslationCurriculumRepository);
    }

    @Test
    void handleUpdateCurriculum_newFileReplacesExistingRow() {
        TrainingTranslationCurriculum existingCurriculum = createExistingCurriculum("vana-nimi.pdf");

        trainingTranslationCurriculumService.handleUpdateCurriculum(trainingTranslation, PDF_BASE64, false, "Õppekava");

        TrainingTranslationCurriculum savedCurriculum = captureSavedCurriculum();
        assertSame(existingCurriculum, savedCurriculum);
        assertArrayEquals(PDF_BYTES, savedCurriculum.getFile());
        assertEquals(PDF_BYTES.length, savedCurriculum.getFileSize());
        assertEquals("java-algkursus-oppekava.pdf", savedCurriculum.getFileName());
    }

    @Test
    void handleUpdateCurriculum_removedDeletesRow() {
        trainingTranslationCurriculumService.handleUpdateCurriculum(trainingTranslation, null, true, "Õppekava");

        verify(trainingTranslationCurriculumRepository).deleteTrainingTranslationCurriculumBy(TRAINING_TRANSLATION_ID);
        verify(trainingTranslationCurriculumRepository, never()).save(any());
    }

    @Test
    void handleUpdateCurriculum_withoutFileRenamesExistingFileAfterTitleChange() {
        TrainingTranslationCurriculum existingCurriculum = createExistingCurriculum("java-alused-oppekava.pdf");
        byte[] existingFile = existingCurriculum.getFile();

        trainingTranslationCurriculumService.handleUpdateCurriculum(trainingTranslation, null, false, "Õppekava");

        TrainingTranslationCurriculum savedCurriculum = captureSavedCurriculum();
        assertEquals("java-algkursus-oppekava.pdf", savedCurriculum.getFileName());
        assertSame(existingFile, savedCurriculum.getFile());
    }

    @Test
    void handleUpdateCurriculum_withoutFileAndSameNameDoesNotLoadFile() {
        createExistingCurriculum("java-algkursus-oppekava.pdf");

        trainingTranslationCurriculumService.handleUpdateCurriculum(trainingTranslation, null, null, "Õppekava");

        verify(trainingTranslationCurriculumRepository, never()).findTrainingTranslationCurriculumBy(any());
        verify(trainingTranslationCurriculumRepository, never()).save(any());
    }

    @Test
    void handleUpdateCurriculum_withoutFileAndWithoutRowDoesNothing() {
        trainingTranslationCurriculumService.handleUpdateCurriculum(trainingTranslation, null, false, "Õppekava");

        verify(trainingTranslationCurriculumRepository, never()).save(any());
        verify(trainingTranslationCurriculumRepository, never()).deleteTrainingTranslationCurriculumBy(any());
    }

    @Test
    void handleAddCurriculum_notPdfThrowsTypeNotAllowed() {
        String pngBase64 = Base64.getEncoder().encodeToString(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n'});

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainingTranslationCurriculumService.handleAddCurriculum(trainingTranslation, pngBase64, "Õppekava"));

        assertEquals("CURRICULUM_TYPE_NOT_ALLOWED", exception.getErrorCode());
        verify(trainingTranslationCurriculumRepository, never()).save(any());
    }

    @Test
    void handleAddCurriculum_tooShortFileThrowsTypeNotAllowed() {
        String shortBase64 = Base64.getEncoder().encodeToString("%PD".getBytes(StandardCharsets.US_ASCII));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainingTranslationCurriculumService.handleAddCurriculum(trainingTranslation, shortBase64, "Õppekava"));

        assertEquals("CURRICULUM_TYPE_NOT_ALLOWED", exception.getErrorCode());
    }

    @Test
    void createValidCurriculum_maxSizePassesAndLargerThrowsTooLarge() {
        assertEquals(TrainingTranslationCurriculumService.MAX_CURRICULUM_BYTES,
                TrainingTranslationCurriculumService.createValidCurriculum(createPdfBase64(TrainingTranslationCurriculumService.MAX_CURRICULUM_BYTES)).length);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> TrainingTranslationCurriculumService.createValidCurriculum(createPdfBase64(TrainingTranslationCurriculumService.MAX_CURRICULUM_BYTES + 1)));

        assertEquals("CURRICULUM_TOO_LARGE", exception.getErrorCode());
    }

    // Päringu suurus: ~10,5 MB fail (Base64-na ~14,7 miljonit märki) peab Jacksonist läbi minema,
    // et vastus oleks 403 CURRICULUM_TOO_LARGE, mitte 400/500 (Jacksoni stringi vaikepiir 20 miljonit märki)
    @Test
    void largeCurriculumJson_isReadByJacksonAndRejectedAsTooLarge() {
        String largePdfBase64 = createPdfBase64(10 * 1024 * 1024 + 512 * 1024);
        String requestJson = "{\"languageId\":2,\"curriculum\":\"" + largePdfBase64 + "\",\"curriculumLabel\":\"Curriculum\"}";

        TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto =
                JsonMapper.builder().build().readValue(requestJson, TrainingTranslationCreateRequestDto.class);

        assertEquals(largePdfBase64.length(), trainingTranslationCreateRequestDto.getCurriculum().length());
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> TrainingTranslationCurriculumService.createValidCurriculum(trainingTranslationCreateRequestDto.getCurriculum()));
        assertEquals("CURRICULUM_TOO_LARGE", exception.getErrorCode());
    }

    private TrainingTranslationCurriculum createExistingCurriculum(String fileName) {
        TrainingTranslationCurriculum existingCurriculum = new TrainingTranslationCurriculum();
        existingCurriculum.setId(5);
        existingCurriculum.setTrainingTranslation(trainingTranslation);
        existingCurriculum.setFile("%PDF-vana".getBytes(StandardCharsets.US_ASCII));
        existingCurriculum.setFileSize(9);
        existingCurriculum.setFileName(fileName);
        when(trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumBy(TRAINING_TRANSLATION_ID))
                .thenReturn(Optional.of(existingCurriculum));
        when(trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumInfoBy(TRAINING_TRANSLATION_ID))
                .thenReturn(Optional.of(createCurriculumInfo(fileName, 9)));
        return existingCurriculum;
    }

    private TrainingTranslationCurriculum captureSavedCurriculum() {
        ArgumentCaptor<TrainingTranslationCurriculum> curriculumCaptor = ArgumentCaptor.forClass(TrainingTranslationCurriculum.class);
        verify(trainingTranslationCurriculumRepository).save(curriculumCaptor.capture());
        return curriculumCaptor.getValue();
    }

    private static TrainingTranslationCurriculumInfo createCurriculumInfo(String fileName, Integer fileSize) {
        return new TrainingTranslationCurriculumInfo() {
            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public Integer getFileSize() {
                return fileSize;
            }
        };
    }

    private static String createPdfBase64(int size) {
        byte[] pdfBytes = new byte[size];
        Arrays.fill(pdfBytes, (byte) ' ');
        System.arraycopy(PDF_BYTES, 0, pdfBytes, 0, 5);
        return Base64.getEncoder().encodeToString(pdfBytes);
    }
}
