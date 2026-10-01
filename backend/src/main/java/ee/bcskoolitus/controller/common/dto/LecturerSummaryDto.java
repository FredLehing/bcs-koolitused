package ee.bcskoolitus.controller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerSummaryDto {

    private Integer lecturerId;
    private String fullName;
    private String title;
    private String shortDescription;
    // lecturer_photo.updated_at epoch-sekundites; null = pilti pole
    private Long photoVersion;
}
