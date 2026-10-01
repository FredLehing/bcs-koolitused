package ee.bcskoolitus.controller.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// GET /api/next-courses query parameetrid (avalehe "järgmised N" toimumiskorda)
@Data
public class NextCourseFilterDto {

    @NotNull
    private String contentLang;

    @NotNull
    @Min(1)
    @Max(20)
    private Integer limit = 5;
}
