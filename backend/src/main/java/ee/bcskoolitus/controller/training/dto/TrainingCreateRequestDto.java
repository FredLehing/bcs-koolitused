package ee.bcskoolitus.controller.training.dto;

import ee.bcskoolitus.infrastructure.validation.HtmlNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingCreateRequestDto implements Serializable {
    @NotNull
    private Integer userId;

    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer trainingLanguageId;

    @NotNull
    private Integer locationId;

    private Integer defaultLecturerId;

    @NotNull
    private Boolean isOrderable;

    @NotNull
    private Boolean isPromoted;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String shortDescription;

    @NotBlank
    @HtmlNotBlank
    private String description;

    @NotNull
    private List<Integer> fundingTypeIds;
}
