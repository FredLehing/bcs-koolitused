package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.trainingtranslation.dto.TrainingTranslationDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingTranslationService {

    private final TrainingTranslationRepository trainingTranslationRepository;
    private final TrainingTranslationMapper trainingTranslationMapper;
    private final TrainingTranslationCurriculumService trainingTranslationCurriculumService;
    private final LanguageService languageService;

    // Koolituse nimi contentLang keeles, puudumisel põhikeeles; tõlke puudumisel ""
    public String getTrainingTitle(Integer trainingId, String contentLang) {
        return trainingTranslationRepository.findByTraining_IdAndLanguage_Code(trainingId, contentLang)
                .or(() -> trainingTranslationRepository.findByTraining_IdAndLanguage_Code(
                        trainingId, languageService.getMainLanguage().getCode()))
                .map(TrainingTranslation::getTitle)
                .orElse("");
    }

    public TrainingTranslationDto getTrainingTranslation(Integer trainingTranslationId) {
        TrainingTranslation trainingTranslation = getValidActiveTrainingTranslationBy(trainingTranslationId);
        TrainingTranslationDto trainingTranslationDto = trainingTranslationMapper.toTrainingTranslationDto(trainingTranslation);
        handleAddCurriculumInfo(trainingTranslationDto, trainingTranslationId);
        return trainingTranslationDto;
    }

    // Õppekava nimi ja suurus ilma faili baitideta; õppekavata tõlkel jäävad väljad null
    private void handleAddCurriculumInfo(TrainingTranslationDto trainingTranslationDto, Integer trainingTranslationId) {
        trainingTranslationCurriculumService.findCurriculumInfo(trainingTranslationId)
                .ifPresent(trainingTranslationCurriculumInfo -> {
                    trainingTranslationDto.setCurriculumFileName(trainingTranslationCurriculumInfo.getFileName());
                    trainingTranslationDto.setCurriculumFileSize(trainingTranslationCurriculumInfo.getFileSize());
                });
    }

    // Kustutatud koolituse (status D) tõlge on nagu olematu → 404
    public TrainingTranslation getValidActiveTrainingTranslationBy(Integer trainingTranslationId) {
        TrainingTranslation trainingTranslation = getValidTrainingTranslationBy(trainingTranslationId);
        if (TrainingStatus.DELETED.getCode().equals(trainingTranslation.getTraining().getStatus())) {
            throw new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId);
        }
        return trainingTranslation;
    }

    public TrainingTranslation getValidTrainingTranslationBy(Integer trainingTranslationId) {
        return trainingTranslationRepository.findById(trainingTranslationId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId));
    }

    // Teisele koolitusele kuuluv tõlge käitub nagu olematu tõlge (404)
    public TrainingTranslation getValidTrainingTranslationBy(Integer trainingTranslationId, Integer trainingId) {
        return trainingTranslationRepository.findTrainingTranslationBy(trainingTranslationId, trainingId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId));
    }
}
