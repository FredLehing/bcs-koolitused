package ee.bcskoolitus.controller.courseparticipant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegistrationDto {
    private Integer courseParticipantId;
    // R = registreerunud, C = loobunud
    private String status;
    private Boolean hasPaid;
    private Boolean requiresLaptop;
    // Osaleja enda lisainfo (tühi string, kui puudub)
    private String notes;
    // Admini märkmed (null, kui puudub)
    private String adminNotes;
    private Instant createdAt;
    private Instant updatedAt;
    private String participantName;
    // Kontakt osaleja profiilist; accountEmail = kasutajakonto e-post
    private String email;
    private String phone;
    private String accountEmail;
    private Integer courseId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    // CourseStatus: U, O, F, X (D = kustutatud)
    private String courseStatus;
    // Toimumiskord on lõppenud (end_date < täna)
    private Boolean isPast;
}
