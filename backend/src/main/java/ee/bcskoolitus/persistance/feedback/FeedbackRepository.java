package ee.bcskoolitus.persistance.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Registreerumisel on üks tagasiside (feedback_uq)
    Optional<Feedback> findByCourseParticipantId(Integer courseParticipantId);

    // "Minu koolitused": millistel registreerumistel on tagasiside olemas
    @Query("select f.courseParticipant.id from Feedback f where f.courseParticipant.id in :courseParticipantIds")
    List<Integer> findFeedbackCourseParticipantIdsBy(List<Integer> courseParticipantIds);
}
