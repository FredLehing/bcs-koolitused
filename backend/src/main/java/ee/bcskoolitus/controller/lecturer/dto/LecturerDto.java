package ee.bcskoolitus.controller.lecturer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDto implements Serializable {
    private Integer lecturerId;
    private String lecturerName;
    private String lecturerPhoto;
}
