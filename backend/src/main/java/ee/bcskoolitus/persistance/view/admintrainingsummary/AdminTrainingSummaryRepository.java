package ee.bcskoolitus.persistance.view.admintrainingsummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminTrainingSummaryRepository extends JpaRepository<AdminTrainingSummary, Long> {

    List<AdminTrainingSummary> findAllByContentLanguageCodeAndStatusNotOrderByTitleAsc(String contentLang, String status);
}
