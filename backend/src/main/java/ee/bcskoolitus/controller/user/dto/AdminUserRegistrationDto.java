package ee.bcskoolitus.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Admini konto vaate registreerumine
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserRegistrationDto {
    private Integer courseParticipantId;
    private Integer courseId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPast;
    // CourseParticipantStatus: R = registreerunud, C = loobunud
    private String status;
    private Boolean hasPaid;
}
