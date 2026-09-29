package ee.bcskoolitus.service;

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

    public TrainingTranslationDto getTrainingTranslation(Integer trainingTranslationId) {
        TrainingTranslation trainingTranslation = getValidTrainingTranslationBy(trainingTranslationId);
        return trainingTranslationMapper.toTrainingTranslationDto(trainingTranslation);
    }

    public TrainingTranslation getValidTrainingTranslationBy(Integer trainingTranslationId) {
        return trainingTranslationRepository.findById(trainingTranslationId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingTranslationId", trainingTranslationId));
    }
}
