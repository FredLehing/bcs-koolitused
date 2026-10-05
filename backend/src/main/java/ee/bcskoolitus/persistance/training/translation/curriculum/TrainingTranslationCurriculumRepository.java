package ee.bcskoolitus.persistance.training.translation.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TrainingTranslationCurriculumRepository extends JpaRepository<TrainingTranslationCurriculum, Integer> {

    @Query("select ttc from TrainingTranslationCurriculum ttc where ttc.trainingTranslation.id = :trainingTranslationId")
    Optional<TrainingTranslationCurriculum> findTrainingTranslationCurriculumBy(Integer trainingTranslationId);

    // Ainult nimi ja suurus — faili baite ei loeta
    @Query("""
            select ttc.fileName as fileName, ttc.fileSize as fileSize
            from TrainingTranslationCurriculum ttc
            where ttc.trainingTranslation.id = :trainingTranslationId""")
    Optional<TrainingTranslationCurriculumInfo> findTrainingTranslationCurriculumInfoBy(Integer trainingTranslationId);

    @Modifying(flushAutomatically = true)
    @Query("delete from TrainingTranslationCurriculum ttc where ttc.trainingTranslation.id = :trainingTranslationId")
    void deleteTrainingTranslationCurriculumBy(Integer trainingTranslationId);
}
