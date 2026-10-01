package ee.bcskoolitus.persistance.course.participant.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseParticipantFeedbackRepository extends JpaRepository<CourseParticipantFeedback, Integer> {

    @Query("select a from CourseParticipantFeedback a join fetch a.feedbackCriteria where a.feedback.id = :feedbackId order by a.id")
    List<CourseParticipantFeedback> findAllByFeedbackId(Integer feedbackId);
}
