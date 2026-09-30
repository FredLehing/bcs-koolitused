package ee.bcskoolitus.persistance.training.lecturer;

import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.training.Training;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "training_lecturer", schema = "bcs_koolitused")
public class TrainingLecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Kuvamise järjekord, 1 = esimene
    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;


}
