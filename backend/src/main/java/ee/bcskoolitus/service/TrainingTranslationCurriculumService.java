package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.infrastructure.util.FileNameSanitizer;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculum;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumInfo;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static ee.bcskoolitus.Error.CURRICULUM_TOO_LARGE;
import static ee.bcskoolitus.Error.CURRICULUM_TYPE_NOT_ALLOWED;

// Koolituse tõlke õppekava (training_translation_curriculum): kontroll, salvestamine, failinimi ja lugemine.
// Salvestamine kutsutakse TrainingService'i transaktsiooni sees pärast tõlke salvestamist.
@Service
@RequiredArgsConstructor
public class TrainingTranslationCurriculumService {

    static final int MAX_CURRICULUM_BYTES = 10 * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final TrainingTranslationCurriculumRepository trainingTranslationCurriculumRepository;

    // Õppekavata tõlge → 404 (sama sõnum nagu olematu tõlke korral)
    public TrainingTranslationCurriculum getValidTrainingTranslationCurriculumBy(Integer trainingTranslationId) {
        return trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumBy(trainingTranslationId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId));
    }

    // Nimi ja suurus ilma faili baitideta
    public Optional<TrainingTranslationCurriculumInfo> findCurriculumInfo(Integer trainingTranslationId) {
        return trainingTranslationCurriculumRepository.findTrainingTranslationCurriculumInfoBy(trainingTranslationId);
    }

    // Uue tõlke õppekava (POST): fail on valikuline, ilma failita rida ei teki
    public void handleAddCurriculum(TrainingTranslation trainingTranslation, String curriculumBase64, String curriculumLabel) {
        if (curriculumBase64 != null) {
            saveCurriculum(trainingTranslation, curriculumBase64, curriculumLabel);
        }
    }

    // Olemasoleva tõlke õppekava (PUT): uus fail → asendus, eemaldus → kustutus,
    // muidu arvutatakse olemasoleva faili nimi uuesti (pealkiri võis muutuda)
    public void handleUpdateCurriculum(TrainingTranslation trainingTranslation, String curriculumBase64,
                                       Boolean isCurriculumRemoved, String curriculumLabel) {
        if (curriculumBase64 != null) {
            saveCurriculum(trainingTranslation, curriculumBase64, curriculumLabel);
        } else if (Boolean.TRUE.equals(isCurriculumRemoved)) {
            trainingTranslationCurriculumRepository.deleteTrainingTranslationCurriculumBy(trainingTranslation.getId());
        } else {
            handleUpdateFileName(trainingTranslation, curriculumLabel);
        }
    }

    // Lisab või asendab faili; Base64 õigsust kontrollib DTO (@ValidBase64)
    private void saveCurriculum(TrainingTranslation trainingTranslation, String curriculumBase64, String curriculumLabel) {
        byte[] curriculumBytes = createValidCurriculum(curriculumBase64);
        TrainingTranslationCurriculum trainingTranslationCurriculum = trainingTranslationCurriculumRepository
                .findTrainingTranslationCurriculumBy(trainingTranslation.getId())
                .orElseGet(TrainingTranslationCurriculum::new);
        trainingTranslationCurriculum.setTrainingTranslation(trainingTranslation);
        trainingTranslationCurriculum.setFile(curriculumBytes);
        trainingTranslationCurriculum.setFileSize(curriculumBytes.length);
        trainingTranslationCurriculum.setFileName(createFileName(trainingTranslation, curriculumLabel));
        trainingTranslationCurriculumRepository.save(trainingTranslationCurriculum);
    }

    // Faili baidid loetakse ainult siis, kui nimi tegelikult muutub
    private void handleUpdateFileName(TrainingTranslation trainingTranslation, String curriculumLabel) {
        String fileName = createFileName(trainingTranslation, curriculumLabel);
        Optional<TrainingTranslationCurriculumInfo> trainingTranslationCurriculumInfo = trainingTranslationCurriculumRepository
                .findTrainingTranslationCurriculumInfoBy(trainingTranslation.getId());
        if (trainingTranslationCurriculumInfo.isEmpty() || fileName.equals(trainingTranslationCurriculumInfo.get().getFileName())) {
            return;
        }
        TrainingTranslationCurriculum trainingTranslationCurriculum = trainingTranslationCurriculumRepository
                .findTrainingTranslationCurriculumBy(trainingTranslation.getId())
                .orElseThrow();
        trainingTranslationCurriculum.setFileName(fileName);
        trainingTranslationCurriculumRepository.save(trainingTranslationCurriculum);
    }

    private static String createFileName(TrainingTranslation trainingTranslation, String curriculumLabel) {
        return FileNameSanitizer.createCurriculumFileName(trainingTranslation.getTitle(), curriculumLabel,
                trainingTranslation.getTraining().getId());
    }

    // Suurus → tüüp (faili algus %PDF-); fail salvestatakse muutmata kujul
    static byte[] createValidCurriculum(String curriculumBase64) {
        byte[] curriculumBytes = Base64.getDecoder().decode(curriculumBase64);
        if (curriculumBytes.length > MAX_CURRICULUM_BYTES) {
            throw new ForbiddenException(CURRICULUM_TOO_LARGE.getMessage(), CURRICULUM_TOO_LARGE.name());
        }
        if (curriculumBytes.length < PDF_SIGNATURE.length
                || !Arrays.equals(curriculumBytes, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length)) {
            throw new ForbiddenException(CURRICULUM_TYPE_NOT_ALLOWED.getMessage(), CURRICULUM_TYPE_NOT_ALLOWED.name());
        }
        return curriculumBytes;
    }
}
