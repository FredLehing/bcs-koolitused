package ee.bcskoolitus.persistance.view.admintrainingsummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// Admini tabeli filtreeritud päring koostatakse AdminTrainingSummarySpecifications abil (valikulised filtrid, muutuv sõnade arv)
public interface AdminTrainingSummaryRepository extends JpaRepository<AdminTrainingSummary, Long>, JpaSpecificationExecutor<AdminTrainingSummary> {

    List<AdminTrainingSummary> findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc(String contentLang, String status);
}
