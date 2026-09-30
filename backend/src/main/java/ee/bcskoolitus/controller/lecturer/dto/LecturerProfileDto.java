package ee.bcskoolitus.controller.lecturer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerProfileDto {

    private Integer lecturerId;
    private String fullName;
    private String title;
    private String shortDescription;
    private String description;
    // lecturer_photo.updated_at epoch-sekundites; null = pilti pole
    private Long photoVersion;
    private List<LecturerTrainingDto> trainings;
}
