package ee.bcskoolitus.persistance.course.lecturer;

import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course_lecturer", schema = "bcs_koolitused")
public class CourseLecturer {
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
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Kuvamise järjekord, 1 = esimene
    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;


}
