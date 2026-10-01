package ee.bcskoolitus.service;

import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedback;
import ee.bcskoolitus.persistance.feedback.criteria.FeedbackCriteria;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FeedbackAnswersVersionServiceTest {
    private final FeedbackAnswersVersionService service = new FeedbackAnswersVersionService();
    @Test
    void contentIdentityAndNanosecondsChangeVersionButInputOrderDoesNot() {
        CourseParticipantFeedback first = answer(1, 1, null);
        CourseParticipantFeedback second = answer(2, 2, "Õpi \\\"SQL\\\"\\n");
        String version = service.getAnswersVersion(3, List.of(first, second));
        assertEquals(version, service.getAnswersVersion(3, List.of(second, first)));
        first.setFeedbackText(""); assertNotEquals(version, service.getAnswersVersion(3, List.of(first, second)));
        first.setFeedbackText(null); first.setId(9); assertNotEquals(version, service.getAnswersVersion(3, List.of(first, second)));
        first.setId(1); first.setUpdatedAt(first.getUpdatedAt().plusNanos(1)); assertNotEquals(version, service.getAnswersVersion(3, List.of(first, second)));
        assertNotEquals(service.getAnswersVersion(3, List.of()), service.getAnswersVersion(4, List.of()));
    }
    private static CourseParticipantFeedback answer(int id, int criterionId, String text) {
        FeedbackCriteria criterion = new FeedbackCriteria(); criterion.setId(criterionId);
        CourseParticipantFeedback answer = new CourseParticipantFeedback(); answer.setId(id); answer.setFeedbackCriteria(criterion);
        answer.setScore(5); answer.setFeedbackText(text); answer.setUpdatedAt(Instant.parse("2026-09-28T11:00:00Z"));
        return answer;
    }
}
