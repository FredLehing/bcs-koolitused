package ee.bcskoolitus.controller.courseparticipant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// "Minu koolitused": kasutaja oma registreerumine koos toimumiskorraga
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRegistrationDto {
    private Integer courseParticipantId;
    private Integer courseId;
    // contentLang keeles, puudumisel põhikeeles
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    // Hübriidkoolitusel mõlemad true
    private Boolean isOnSite;
    private Boolean isOnline;
    // CourseStatus: U, O, F, X
    private String courseStatus;
    // CourseParticipantStatus: R = registreerunud, C = loobunud
    private String status;
    private Boolean hasPaid;
    private Boolean isPast;
    // R, toimumiskord pole alanud ega tühistatud
    private Boolean canCancel;
}
