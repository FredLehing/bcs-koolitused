package ee.bcskoolitus.controller.lecturer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerCreateResponseDto {

    private Integer lecturerId;
    private Integer lecturerTranslationId;
}
