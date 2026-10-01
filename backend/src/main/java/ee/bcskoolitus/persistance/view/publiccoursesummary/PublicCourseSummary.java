package ee.bcskoolitus.persistance.view.publiccoursesummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

// View public_course_summary: üks rida toimumiskorra ja olemasoleva tõlke kohta (nagu training_summary).
// Ainult publitseeritud koolituse avatud või täis (O, F) tulevased toimumiskorrad.
@Getter
@Entity
@Immutable
@Table(name = "public_course_summary", schema = "bcs_koolitused")
public class PublicCourseSummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "content_language_code", nullable = false, length = 2)
    private String contentLanguageCode;

    @Column(name = "training_id", nullable = false)
    private Integer trainingId;

    @Column(name = "training_translation_id", nullable = false)
    private Integer trainingTranslationId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "training_language_id", nullable = false)
    private Integer trainingLanguageId;

    @Column(name = "training_language_flag_icon_code", nullable = false, length = 10)
    private String trainingLanguageFlagIconCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "number_of_days", nullable = false)
    private Integer numberOfDays;

    @Column(name = "number_of_academic_hours", nullable = false)
    private Integer numberOfAcademicHours;

    @Column(name = "price", nullable = false, precision = 9, scale = 4)
    private BigDecimal price;

    @Column(name = "status", nullable = false, length = 3)
    private String status;

    @Column(name = "is_promoted", nullable = false)
    private Boolean isPromoted;

    // Ruum määratud
    @Column(name = "is_on_site", nullable = false)
    private Boolean isOnSite;

    // Veebilink määratud
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline;

    // Koolitajad sort_order järjekorras, nt "Rain Tüür, Meelis Teern"; null = koolitajaid pole
    @Column(name = "lecturer_names")
    private String lecturerNames;
}
