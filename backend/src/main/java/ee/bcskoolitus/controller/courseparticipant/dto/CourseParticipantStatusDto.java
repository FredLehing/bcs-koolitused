package ee.bcskoolitus.controller.courseparticipant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseParticipantStatusDto {
    // R = registreerunud, C = loobunud, null = pole registreerunud
    private String status;
}
