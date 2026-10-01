package ee.bcskoolitus.controller.enquiry.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnquiryCreateRequestDto {

    // Publitseeritud koolitus
    @NotNull
    private Integer trainingId;

    // null = üldine päring koolituse kohta; muidu selle koolituse avatud või täis toimumiskord
    private Integer courseId;

    @NotBlank
    @Size(max = 255)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    // Tühi → null
    @Size(max = 255)
    private String companyName;

    @NotBlank
    @Size(max = 255)
    private String message;
}
