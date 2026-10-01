package ee.bcskoolitus.controller.courseparticipant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegistrationSummaryDto {
    private Integer courseParticipantId;
    private Instant registeredAt;
    private String participantName;
    // Kontakt osaleja profiilist
    private String email;
    private Integer courseId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    // Toimumiskord on lõppenud (end_date < täna)
    private Boolean isPast;
    private Boolean hasPaid;
    private Boolean requiresLaptop;
    // R = registreerunud, C = loobunud
    private String status;
}
