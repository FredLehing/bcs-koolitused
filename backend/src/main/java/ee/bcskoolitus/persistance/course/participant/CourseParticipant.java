package ee.bcskoolitus.persistance.course.participant;

import ee.bcskoolitus.persistance.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
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
    @Column(name = "notes", nullable = false, length = Integer.MAX_VALUE)
    private String notes;

    @NotNull
    @Column(name = "has_paid", nullable = false)
    private Boolean hasPaid;

    @NotNull
    @Column(name = "requires_laptop", nullable = false)
    private Boolean requiresLaptop;

    @Size(max = 3)
    @NotNull
    @Column(name = "status", nullable = false, length = 3)
    private String status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}