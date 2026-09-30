package ee.bcskoolitus.persistance.view.adminlecturersummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminLecturerSummaryRepository extends JpaRepository<AdminLecturerSummary, Long> {

    List<AdminLecturerSummary> findAllByContentLanguageCodeOrderByFullNameAscLecturerIdAsc(String contentLang);

    List<AdminLecturerSummary> findAllByContentLanguageCodeAndStatusOrderByFullNameAscLecturerIdAsc(String contentLang, String status);
}
