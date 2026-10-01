package ee.bcskoolitus.controller.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// GET /api/admin-courses query parameetrid. Kohustuslikud väljad on @NotNull,
// valikulised null (ID filtritel ka 0) = filtrit ei rakendata.
@Data
public class AdminCourseFilterDto {

    @NotNull
    private String contentLang;

    // null või tühi = otsingut ei rakendata; iga sõna peab esinema koolituse nimes
    private String searchText;

    private Integer categoryId;

    private Integer trainingLanguageId;

    // "U", "O", "F" või "X"; null = kõik peale kustutatud (D)
    @Pattern(regexp = "[UOFX]", message = "peab olema U, O, F või X")
    private String status;

    // ONSITE = ruum määratud, ONLINE = veebilink määratud (hübriid sobib mõlemaga)
    @Pattern(regexp = "ONSITE|ONLINE", message = "peab olema ONSITE või ONLINE")
    private String attendance;

    private Boolean isPromoted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    // false = ainult tulevased ja käimasolevad (end_date >= täna)
    private Boolean includePast = false;

    // startDate / trainingTitle / price / status / participantCount / enquiryCount;
    // puudub või tundmatu → vaikimisi järjestus (tulevased lähimast, siis möödunud hiliseimast)
    private String sortBy;

    // "ASC"; kõik muu → DESC (tõstutundetu)
    private String sortDirection;

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer limit;
}
