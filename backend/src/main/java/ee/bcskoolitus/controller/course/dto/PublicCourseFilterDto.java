package ee.bcskoolitus.controller.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// GET /api/courses query parameetrid. ID filtrite 0 (või puudumine) ja valikuliste filtrite null = filtrit ei rakendata.
@Data
public class PublicCourseFilterDto {

    @NotNull
    private String contentLang;

    // "" või null = otsingut ei rakendata; iga sõna peab esinema pealkirjas või lühikirjelduses
    private String searchText;

    private Integer categoryId;

    private Integer trainingLanguageId;

    private Integer fundingTypeId;

    // ONSITE = ruum määratud, ONLINE = veebilink määratud (hübriid sobib mõlemaga)
    @Pattern(regexp = "ONSITE|ONLINE", message = "peab olema ONSITE või ONLINE")
    private String attendance;

    // true = täis (F) toimumiskordi ei näidata
    private Boolean hideFull = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer limit;
}
