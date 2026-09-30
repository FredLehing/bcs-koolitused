package ee.bcskoolitus.controller.lecturer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDetailDto {

    private Integer lecturerId;
    private String fullName;
    // lecturer_photo.updated_at epoch-sekundites; null = pilti pole
    private Long photoVersion;
}
