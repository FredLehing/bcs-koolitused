package ee.bcskoolitus.controller.lecturertranslation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerTranslationDto {

    private Integer lecturerTranslationId;
    private Integer lecturerId;
    private Integer languageId;
    private String languageCode;
    private String title;
    private String shortDescription;
    private String description;
}
