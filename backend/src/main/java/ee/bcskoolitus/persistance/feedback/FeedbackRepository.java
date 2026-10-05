package ee.bcskoolitus.persistance.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Feedback f where f.id = :feedbackId")
    Optional<Feedback> findFeedbackForUpdateBy(Integer feedbackId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Feedback f where f.courseParticipant.id = :courseParticipantId")
    Optional<Feedback> findFeedbackForUpdateByCourseParticipantId(Integer courseParticipantId);

    // Registreerumisel on üks tagasiside (feedback_uq)
    Optional<Feedback> findByCourseParticipantId(Integer courseParticipantId);

    // "Minu koolitused": millistel registreerumistel on tagasiside olemas
    @Query("select f.courseParticipant.id from Feedback f where f.courseParticipant.id in :courseParticipantIds")
    List<Integer> findFeedbackCourseParticipantIdsBy(List<Integer> courseParticipantIds);
}
