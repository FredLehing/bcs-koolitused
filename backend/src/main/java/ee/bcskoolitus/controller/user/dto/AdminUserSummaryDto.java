package ee.bcskoolitus.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Admini kontode nimekiri; täidetakse UserRepository konstruktori avaldisega. Parooli ei tagastata.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserSummaryDto {
    private Integer userId;
    private String email;
    // admin / participant
    private String roleName;
    // Osaleja puudumisel (tavaliselt adminikontod) null
    private String participantName;
    private String phone;
    // Registreerunud (R) registreerumiste arv, ka toimunud toimumiskordadel
    private Long registrationCount;
    private Instant createdAt;
    // ApiStatus: A = aktiivne, D = deaktiveeritud
    private String status;
}
