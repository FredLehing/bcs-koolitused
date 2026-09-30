package ee.bcskoolitus.controller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Jagatud: GET /api/lecturers ja TrainingDto.lecturers
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDto implements Serializable {
    private Integer lecturerId;
    private String lecturerName;
}
