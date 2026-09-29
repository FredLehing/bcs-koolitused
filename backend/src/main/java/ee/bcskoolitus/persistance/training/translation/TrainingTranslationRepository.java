package ee.bcskoolitus.persistance.training.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainingTranslationRepository extends JpaRepository<TrainingTranslation, Integer> {

    List<TrainingTranslation> findAllByTraining_IdOrderByLanguage_IdAsc(Integer trainingId);

    @Query("select tt from TrainingTranslation tt where tt.id = :trainingTranslationId and tt.training.id = :trainingId")
    Optional<TrainingTranslation> findTrainingTranslationBy(Integer trainingTranslationId, Integer trainingId);
}
