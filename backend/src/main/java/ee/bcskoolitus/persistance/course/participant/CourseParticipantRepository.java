package ee.bcskoolitus.persistance.course.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, Integer> {

    // Osalejal on üks rida toimumiskorra kohta (course_participant_uq)
    Optional<CourseParticipant> findByCourseIdAndParticipantId(Integer courseId, Integer participantId);

    // Toimumiskorra osalejad registreerumise järjekorras (ka loobunud)
    List<CourseParticipant> findAllByCourseIdOrderByCreatedAtAsc(Integer courseId);

    // "Minu koolitused": osaleja registreerumised koos toimumiskorra ja koolitusega, kustutatud toimumiskorrad välja
    @Query("""
            select cp from CourseParticipant cp
            join fetch cp.course c
            join fetch c.training
            where cp.participant.id = :participantId
              and c.status <> :courseDeletedStatus
            order by c.startDate asc, cp.id asc""")
    List<CourseParticipant> findParticipantCourseParticipantsBy(Integer participantId, String courseDeletedStatus);
}
