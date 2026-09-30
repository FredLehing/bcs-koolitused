package ee.bcskoolitus.persistance.course.lecturer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CourseLecturerRepository extends JpaRepository<CourseLecturer, Integer> {

    // Tulevased toimumiskorrad: end_date >= today ja staatus pole ignoredStatuses hulgas (tühistatud, kustutatud)
    @Query("""
            select count(cl) from CourseLecturer cl
            where cl.lecturer.id = :lecturerId
              and cl.course.endDate >= :today
              and cl.course.status not in :ignoredStatuses""")
    long countUpcomingCourseLecturersBy(Integer lecturerId, LocalDate today, List<String> ignoredStatuses);

    // Koolitaja laetakse kohe kaasa (nimi läheb vastusesse)
    @Query("""
            select cl from CourseLecturer cl
            join fetch cl.lecturer
            where cl.course.id = :courseId
            order by cl.sortOrder""")
    List<CourseLecturer> findCourseLecturersBy(Integer courseId);

    // Kustutab kohe andmebaasist — samad paarid saab samas transaktsioonis uuesti lisada ilma course_lecturer_uq veata
    @Modifying(flushAutomatically = true)
    @Query("delete from CourseLecturer cl where cl.course.id = :courseId")
    void deleteCourseLecturersBy(Integer courseId);
}
