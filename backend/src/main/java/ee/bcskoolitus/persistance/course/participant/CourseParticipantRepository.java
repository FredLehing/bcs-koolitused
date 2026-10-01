package ee.bcskoolitus.persistance.course.participant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, Integer> {

    // Osalejal on üks rida toimumiskorra kohta (course_participant_uq)
    Optional<CourseParticipant> findByCourseIdAndParticipantId(Integer courseId, Integer participantId);

    // Toimumiskorra osalejad registreerumise järjekorras (ka loobunud)
    List<CourseParticipant> findAllByCourseIdOrderByCreatedAtAsc(Integer courseId);
}
