package ee.bcskoolitus.persistance.course.participant.feedback;

import ee.bcskoolitus.persistance.feedback.Feedback;
import ee.bcskoolitus.persistance.feedback.criteria.FeedbackCriteria;
import jakarta.persistence.*;
import ee.bcskoolitus.persistance.feedback.FeedbackTimestampConverter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// Tagasiside vastus: ühe kriteeriumi hinne (1–10) ja valikuline kommentaar
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "course_participant_feedback", schema = "bcs_koolitused")
public class CourseParticipantFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_criteria_id", nullable = false)
    private FeedbackCriteria feedbackCriteria;

    @NotNull
    @Column(name = "score", nullable = false)
    private Integer score;

    // null = kommentaari pole
    @Size(max = 10000)
    @Column(name = "feedback_text", columnDefinition = "text")
    private String feedbackText;

    @NotNull
    @CreatedDate
    @Convert(converter = FeedbackTimestampConverter.class)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @LastModifiedDate
    @Convert(converter = FeedbackTimestampConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
