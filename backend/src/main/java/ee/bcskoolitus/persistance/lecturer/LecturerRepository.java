package ee.bcskoolitus.persistance.lecturer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    @Query("""
            select l from Lecturer l
            where lower(l.fullName) like lower(concat('%', :search, '%'))
            order by l.fullName""")
    List<Lecturer> findLecturersBy(String search);
}
