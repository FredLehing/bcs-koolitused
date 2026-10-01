package ee.bcskoolitus.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Registreerumise vormi eeltäitmine; osalejata kasutajal participantId = null, nimed ja telefon "", email = user.email
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyParticipantDto {
    private Integer participantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
