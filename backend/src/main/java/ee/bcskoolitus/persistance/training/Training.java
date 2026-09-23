package ee.bcskoolitus.persistance.training;

import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.location.Location;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "training", schema = "bcs_koolitused")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_lecturer_id")
    private Lecturer defaultLecturer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_language_id", nullable = false)
    private Language trainingLanguage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @NotNull
    @Column(name = "is_order_only", nullable = false)
    private Boolean isOrderOnly;

    @NotNull
    @Column(name = "is_promoted", nullable = false)
    private Boolean isPromoted;


}