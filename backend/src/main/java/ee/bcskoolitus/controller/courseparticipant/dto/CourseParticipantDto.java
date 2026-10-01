package ee.bcskoolitus.controller.courseparticipant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseParticipantDto {
    private Integer courseParticipantId;
    private String participantName;
    // Kontaktandmed osaleja profiilist
    private String email;
    private String phone;
    private Instant registeredAt;
    private Boolean hasPaid;
    private Boolean requiresLaptop;
    // R = registreerunud, C = loobunud
    private String status;
    private String notes;
}
