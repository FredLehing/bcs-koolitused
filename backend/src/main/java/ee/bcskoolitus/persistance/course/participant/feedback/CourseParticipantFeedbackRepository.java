package ee.bcskoolitus.persistance.course.participant.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseParticipantFeedbackRepository extends JpaRepository<CourseParticipantFeedback, Integer> {

    List<CourseParticipantFeedback> findAllByFeedbackId(Integer feedbackId);
}
