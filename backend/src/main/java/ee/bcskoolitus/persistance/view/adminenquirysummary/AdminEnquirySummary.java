package ee.bcskoolitus.persistance.view.adminenquirysummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.time.LocalDate;

// View admin_enquiry_summary: üks rida päringu ja tõlkekeele (content_language_code) kohta,
// puuduva tõlke korral koolituse ja osalemisvormi nimi põhikeeles
@Getter
@Entity
@Immutable
@Table(name = "admin_enquiry_summary", schema = "bcs_koolitused")
public class AdminEnquirySummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "enquiry_id", nullable = false)
    private Integer enquiryId;

    @Column(name = "content_language_code", nullable = false, length = 2)
    private String contentLanguageCode;

    @Column(name = "training_id", nullable = false)
    private Integer trainingId;

    @Column(name = "training_translation_id", nullable = false)
    private Integer trainingTranslationId;

    @Column(name = "training_title", nullable = false)
    private String trainingTitle;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "course_start_date")
    private LocalDate courseStartDate;

    @Column(name = "course_end_date")
    private LocalDate courseEndDate;

    @Column(name = "option_name")
    private String optionName;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
