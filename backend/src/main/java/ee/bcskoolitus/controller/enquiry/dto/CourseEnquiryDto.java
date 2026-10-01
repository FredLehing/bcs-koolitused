package ee.bcskoolitus.controller.enquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnquiryDto {
    private Integer enquiryId;
    private Instant createdAt;
    private String fullName;
    private String email;
    private String companyName;
    // U = uus, H = käsitletud
    private String status;
}
