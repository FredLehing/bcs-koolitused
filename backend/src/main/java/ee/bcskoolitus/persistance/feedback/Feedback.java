package ee.bcskoolitus.persistance.feedback;

import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// Ühe registreerumise tagasiside "päis"; vastused on course_participant_feedback tabelis
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feedback", schema = "bcs_koolitused")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_participant_id", nullable = false)
    private CourseParticipant courseParticipant;

    // FeedbackStatus: N, U, H
    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
