package ee.bcskoolitus.persistance.training.translation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingTranslationRepository extends JpaRepository<TrainingTranslation, Integer> {

    List<TrainingTranslation> findAllByTraining_IdOrderByLanguage_IdAsc(Integer trainingId);
}
