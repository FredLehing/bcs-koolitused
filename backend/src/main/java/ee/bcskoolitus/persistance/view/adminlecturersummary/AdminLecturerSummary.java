package ee.bcskoolitus.persistance.view.adminlecturersummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

// View admin_lecturer_summary: üks rida koolitaja ja tõlkekeele (content_language_code) kohta,
// puuduva tõlke korral põhikeele ametinimetus. Sisaldab ka kustutatud koolitajaid (status D).
@Getter
@Entity
@Immutable
@Table(name = "admin_lecturer_summary", schema = "bcs_koolitused")
public class AdminLecturerSummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Integer lecturerId;

    @Column(name = "content_language_code", nullable = false, length = 2)
    private String contentLanguageCode;

    @Column(name = "lecturer_translation_id")
    private Integer lecturerTranslationId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "title")
    private String title;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    // Komaga eraldatud puuduvate tõlgete keelekoodid (nt "en"); null = kõik tõlked olemas
    @Column(name = "missing_translation_language_codes")
    private String missingTranslationLanguageCodes;

    @Column(name = "has_all_translations", nullable = false)
    private Boolean hasAllTranslations;

    // Aktiivsed koolitused (training.status <> 'D') training_lecturer kaudu
    @Column(name = "training_count", nullable = false)
    private Long trainingCount;

    // Toimumiskorrad course_lecturer kaudu: end_date >= täna, status NOT IN ('D', 'X')
    @Column(name = "upcoming_course_count", nullable = false)
    private Long upcomingCourseCount;

    // Hiliseim lecturer, lecturer_translation ja lecturer_photo updated_at
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
