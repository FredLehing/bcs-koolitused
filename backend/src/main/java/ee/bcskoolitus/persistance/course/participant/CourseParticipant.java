package ee.bcskoolitus.persistance.course.participant;

import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.participant.Participant;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "course_participant", schema = "bcs_koolitused")
public class CourseParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @NotNull
    @Column(name = "notes", nullable = false, length = Integer.MAX_VALUE)
    private String notes;

    @NotNull
    @Column(name = "has_paid", nullable = false)
    private Boolean hasPaid;

    @NotNull
    @Column(name = "requires_laptop", nullable = false)
    private Boolean requiresLaptop;

    // CourseParticipantStatus: R = registreerunud, C = loobunud
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