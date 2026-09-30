package ee.bcskoolitus.persistance.view.admintrainingsummary;

import ee.bcskoolitus.persistance.training.Training;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

// View admin_training_summary: üks rida koolituse ja tõlkekeele (content_language_code) kohta,
// puuduva tõlke korral põhikeele pealkiri ja kategooria. Sisaldab ka kustutatud koolitusi (status D).
@Getter
@Entity
@Immutable
@Table(name = "admin_training_summary", schema = "bcs_koolitused")
public class AdminTrainingSummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "content_language_code", nullable = false, length = 2)
    private String contentLanguageCode;

    @Column(name = "training_translation_id", nullable = false)
    private Integer trainingTranslationId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "training_language_id", nullable = false)
    private Integer trainingLanguageId;

    @Column(name = "training_language_code", nullable = false, length = 2)
    private String trainingLanguageCode;

    @Column(name = "training_language_flag_icon_code", nullable = false, length = 10)
    private String trainingLanguageFlagIconCode;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    // Sorteerimiseks töövoo järjekorras: 1 = U, 2 = P, 3 = D
    @Column(name = "status_order", nullable = false)
    private Integer statusOrder;

    @Column(name = "is_orderable", nullable = false)
    private Boolean isOrderable;

    @Column(name = "is_promoted", nullable = false)
    private Boolean isPromoted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Hiliseim training.updated_at ja selle koolituse tõlgete updated_at
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Komaga eraldatud puuduvate tõlgete keelekoodid (nt "en"); null = kõik tõlked olemas
    @Column(name = "missing_translation_language_codes")
    private String missingTranslationLanguageCodes;

    @Column(name = "has_all_translations", nullable = false)
    private Boolean hasAllTranslations;
}
