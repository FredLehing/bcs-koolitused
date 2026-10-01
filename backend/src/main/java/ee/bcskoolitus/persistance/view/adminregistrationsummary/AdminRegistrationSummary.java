package ee.bcskoolitus.persistance.view.adminregistrationsummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.time.LocalDate;

// View admin_registration_summary: üks rida registreerumise (course_participant) ja tõlkekeele (content_language_code) kohta,
// puuduva tõlke korral koolituse nimi põhikeeles
@Getter
@Entity
@Immutable
@Table(name = "admin_registration_summary", schema = "bcs_koolitused")
public class AdminRegistrationSummary {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "course_participant_id", nullable = false)
    private Integer courseParticipantId;

    @Column(name = "content_language_code", nullable = false, length = 2)
    private String contentLanguageCode;

    // CourseParticipantStatus: R = registreerunud, C = loobunud
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "has_paid", nullable = false)
    private Boolean hasPaid;

    @Column(name = "requires_laptop", nullable = false)
    private Boolean requiresLaptop;

    @Column(name = "notes", nullable = false)
    private String notes;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "participant_name", nullable = false)
    private String participantName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "account_email", nullable = false)
    private String accountEmail;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "training_title", nullable = false)
    private String trainingTitle;

    @Column(name = "training_status", nullable = false, length = 1)
    private String trainingStatus;

    @Column(name = "course_start_date", nullable = false)
    private LocalDate courseStartDate;

    @Column(name = "course_end_date", nullable = false)
    private LocalDate courseEndDate;

    @Column(name = "course_status", nullable = false, length = 1)
    private String courseStatus;

    @Column(name = "is_past", nullable = false)
    private Boolean isPast;
}
