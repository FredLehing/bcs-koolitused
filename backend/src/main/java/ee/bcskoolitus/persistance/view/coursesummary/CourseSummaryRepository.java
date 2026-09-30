package ee.bcskoolitus.persistance.view.coursesummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSummaryRepository extends JpaRepository<CourseSummary, Integer> {

    // Kõik (ka möödunud); järjestus: tulevased enne möödunuid, lähimast / hiliseimast
    List<CourseSummary> findAllByTrainingIdAndStatusNotOrderByIsPastAscDaysFromTodayAscCourseIdAsc(Integer trainingId, String status);

    List<CourseSummary> findAllByTrainingIdAndStatusNotAndIsPastFalseOrderByDaysFromTodayAscCourseIdAsc(Integer trainingId, String status);
}
