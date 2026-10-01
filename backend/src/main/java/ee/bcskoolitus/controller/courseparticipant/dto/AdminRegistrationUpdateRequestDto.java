package ee.bcskoolitus.controller.courseparticipant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegistrationUpdateRequestDto {

    // R = registreerunud, C = loobunud
    @NotNull
    @Pattern(regexp = "[RC]", message = "lubatud on R või C")
    private String status;

    @NotNull
    private Boolean hasPaid;

    @NotNull
    private Boolean requiresLaptop;

    // Trimmitakse, tühi → null (CourseParticipantService)
    private String adminNotes;
}
