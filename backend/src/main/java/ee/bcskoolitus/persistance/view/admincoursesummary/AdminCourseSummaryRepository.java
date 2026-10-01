package ee.bcskoolitus.persistance.view.admincoursesummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// Admini tabeli filtreeritud päring koostatakse AdminCourseSummarySpecifications abil (valikulised filtrid, muutuv sõnade arv)
public interface AdminCourseSummaryRepository extends JpaRepository<AdminCourseSummary, Long>, JpaSpecificationExecutor<AdminCourseSummary> {

    Optional<AdminCourseSummary> findByCourseIdAndContentLanguageCode(Integer courseId, String contentLang);
}
