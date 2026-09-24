package ee.bcskoolitus.persistance.view.trainingsummary;

import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "training_summary", schema = "bcs_koolitused")
public class TrainingSummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_translation_id", nullable = false)
    private TrainingTranslation trainingTranslation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "training_language_id", nullable = false)
    private Integer trainingLanguageId;

    @Column(name = "training_language_code", nullable = false, length = 2)
    private String trainingLanguageCode;

    @Column(name = "translation_language_id", nullable = false)
    private Integer translationLanguageId;

    @Column(name = "translation_language_code", nullable = false, length = 2)
    private String translationLanguageCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;


}
