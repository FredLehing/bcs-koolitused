package ee.bcskoolitus.persistance.view.publiccoursesummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// Avaliku kalendri filtreeritud päring koostatakse PublicCourseSummarySpecifications abil
public interface PublicCourseSummaryRepository extends JpaRepository<PublicCourseSummary, Long>, JpaSpecificationExecutor<PublicCourseSummary> {

    // Sama koolituse avalikud tulevased toimumiskorrad (toimumiskorra lehe lingid)
    List<PublicCourseSummary> findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(Integer trainingId, String contentLang);
}
