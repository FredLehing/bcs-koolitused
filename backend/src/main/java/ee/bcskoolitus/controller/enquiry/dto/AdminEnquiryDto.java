package ee.bcskoolitus.controller.enquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEnquiryDto {

    private Integer enquiryId;
    private Instant createdAt;
    private String status;
    private Integer trainingId;
    private Integer trainingTranslationId;
    private String trainingTitle;
    private Integer courseId;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private String optionName;
    private String companyName;
    private String message;
    private String fullName;
    private String email;
    private String phone;
}
