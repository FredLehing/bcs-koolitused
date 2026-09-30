package ee.bcskoolitus.persistance.view.coursesummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

// View course_summary: üks rida toimumiskorra kohta koos koolitajate nimede, ruumi ja osalejate arvuga.
// Sisaldab ka kustutatud toimumiskordi (status D) — need välistab päring.
@Getter
@Entity
@Immutable
@Table(name = "course_summary", schema = "bcs_koolitused")
public class CourseSummary {
    @Id
    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "training_id", nullable = false)
    private Integer trainingId;

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

    @Column(name = "number_of_academic_hours", nullable = false)
    private Integer numberOfAcademicHours;

    @Column(name = "price", nullable = false, precision = 9, scale = 4)
    private BigDecimal price;

    @Column(name = "status", nullable = false, length = 3)
    private String status;

    // Koolitajate nimed komadega sort_order järjekorras; null = koolitajaid pole
    @Column(name = "lecturer_names")
    private String lecturerNames;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "has_notes", nullable = false)
    private Boolean hasNotes;

    @Column(name = "has_meeting_link", nullable = false)
    private Boolean hasMeetingLink;

    @Column(name = "participant_count", nullable = false)
    private Long participantCount;
}
