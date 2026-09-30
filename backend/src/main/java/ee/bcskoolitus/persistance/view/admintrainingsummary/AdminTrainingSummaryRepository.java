package ee.bcskoolitus.persistance.view.admintrainingsummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

// Admini tabeli filtreeritud päring koostatakse AdminTrainingSummarySpecifications abil (valikulised filtrid, muutuv sõnade arv)
public interface AdminTrainingSummaryRepository extends JpaRepository<AdminTrainingSummary, Long>, JpaSpecificationExecutor<AdminTrainingSummary> {

    List<AdminTrainingSummary> findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc(String contentLang, String status);

    Optional<AdminTrainingSummary> findByTraining_IdAndContentLanguageCode(Integer trainingId, String contentLang);

    // Koolitaja profiili koolitused (trainingIds training_lecturer kaudu)
    List<AdminTrainingSummary> findAllByContentLanguageCodeAndStatusAndTraining_IdInOrderByTitleAsc(String contentLang, String status, List<Integer> trainingIds);
}
