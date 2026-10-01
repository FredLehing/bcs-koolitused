package ee.bcskoolitus.persistance.view.admincoursesummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

// View admin_course_summary: üks rida toimumiskorra ja tõlkekeele (content_language_code) kohta,
// koolituse nimi puudumisel põhikeeles. Sisaldab ka kustutatud toimumiskordi ja koolitusi (status D) — need välistab päring.
@Getter
@Entity
@Immutable
@Table(name = "admin_course_summary", schema = "bcs_koolitused")
public class AdminCourseSummary {
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

    @Column(name = "training_title", nullable = false)
    private String trainingTitle;

    @Column(name = "training_status", nullable = false, length = 1)
    private String trainingStatus;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "training_language_id", nullable = false)
    private Integer trainingLanguageId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // end_date < täna
    @Column(name = "is_past", nullable = false)
    private Boolean isPast;

    // Sorteerimiseks: tulevased lähimast, möödunud hiliseimast
    @Column(name = "days_from_today", nullable = false)
    private Integer daysFromToday;

    @Column(name = "number_of_days", nullable = false)
    private Integer numberOfDays;

    @Column(name = "price", nullable = false, precision = 9, scale = 4)
    private BigDecimal price;

    @Column(name = "status", nullable = false, length = 3)
    private String status;

    // Sorteerimiseks töövoo järjekorras: 1 = U, 2 = O, 3 = F, 4 = muu
    @Column(name = "status_order", nullable = false)
    private Integer statusOrder;

    @Column(name = "is_promoted", nullable = false)
    private Boolean isPromoted;

    // Ruum määratud
    @Column(name = "is_on_site", nullable = false)
    private Boolean isOnSite;

    @Column(name = "has_meeting_link", nullable = false)
    private Boolean hasMeetingLink;

    // Ainult registreerunud (R)
    @Column(name = "participant_count", nullable = false)
    private Long participantCount;

    // Registreerunud (R), kes on tasunud
    @Column(name = "paid_count", nullable = false)
    private Long paidCount;

    @Column(name = "enquiry_count", nullable = false)
    private Long enquiryCount;
}
