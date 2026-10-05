package ee.bcskoolitus.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

// Admini konto vaade: konto + osaleja (puudumisel osaleja väljad null, registrations tühi). Parooli ei tagastata.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDto {
    private Integer userId;
    private String email;
    private String roleName;
    // ApiStatus: A = aktiivne, D = deaktiveeritud
    private String status;
    private Instant createdAt;
    private Integer participantId;
    private String participantName;
    private String profileEmail;
    private String phone;
    private List<AdminUserRegistrationDto> registrations;
}
