package ee.bcskoolitus.controller.lecturer.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLecturerSummaryDto {

    private Integer lecturerId;
    private Integer lecturerTranslationId;
    private String fullName;
    private String title;
    private String status;
    private Boolean hasAllTranslations;
    private List<String> missingTranslationLanguageCodes;
    private Long trainingCount;
    private Long upcomingCourseCount;
    private Instant updatedAt;
}
