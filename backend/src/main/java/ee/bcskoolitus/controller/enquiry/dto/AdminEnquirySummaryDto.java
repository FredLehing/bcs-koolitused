package ee.bcskoolitus.controller.enquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEnquirySummaryDto {

    private Integer enquiryId;
    private Instant createdAt;
    private String fullName;
    private String email;
    private String companyName;
    private String trainingTitle;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private String status;
}
