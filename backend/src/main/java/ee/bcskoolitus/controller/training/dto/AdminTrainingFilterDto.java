package ee.bcskoolitus.controller.training.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// GET /api/admin-trainings query parameetrid. Kohustuslikud väljad on @NotNull,
// valikulised (status, isOrderable, isPromoted, hasAllTranslations) null = filtrit ei rakendata.
@Data
public class AdminTrainingFilterDto {

    @NotNull
    private String contentLang;

    // "" = otsingut ei rakendata; iga sõna peab esinema pealkirjas
    @NotNull
    private String searchText;

    // 0 = kõik
    @NotNull
    private Integer categoryId;

    // 0 = kõik
    @NotNull
    private Integer trainingLanguageId;

    // 0 = kõik
    @NotNull
    private Integer fundingTypeId;

    // "U", "P" või "D"; null = aktiivsed (U ja P)
    @Pattern(regexp = "[UPD]")
    private String status;

    private Boolean isOrderable;

    private Boolean isPromoted;

    private Boolean hasAllTranslations;

    // createdAt / updatedAt / title / categoryName / trainingLanguageCode / status / hasAllTranslations;
    // tundmatu väärtus → createdAt
    @NotNull
    private String sortBy;

    // "asc"; kõik muu → desc
    @NotNull
    private String sortDirection;

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer limit;
}
