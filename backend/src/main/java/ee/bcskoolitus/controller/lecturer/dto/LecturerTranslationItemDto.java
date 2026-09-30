package ee.bcskoolitus.controller.lecturer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerTranslationItemDto {

    private Integer lecturerTranslationId;
    private Integer languageId;
    private String languageCode;
    private Boolean isMainLanguage;
}
