package ee.bcskoolitus.persistance.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("""
            select count(c) from Course c
            where c.room.id = :roomId
              and c.endDate >= :today
              and c.status not in :ignoredStatuses""")
    long countUpcomingCoursesBy(Integer roomId, LocalDate today, List<String> ignoredStatuses);
}
