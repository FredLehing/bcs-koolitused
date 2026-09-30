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

    // Kustutatud koolituse (status D) tõlge on nagu olematu → 404
    public TrainingTranslationDto getTrainingTranslation(Integer trainingTranslationId) {
        TrainingTranslation trainingTranslation = getValidTrainingTranslationBy(trainingTranslationId);
        if (TrainingStatus.DELETED.getCode().equals(trainingTranslation.getTraining().getStatus())) {
            throw new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId);
        }
        return trainingTranslationMapper.toTrainingTranslationDto(trainingTranslation);
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
