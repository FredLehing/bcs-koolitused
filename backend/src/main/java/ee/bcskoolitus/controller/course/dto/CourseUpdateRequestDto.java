package ee.bcskoolitus.controller.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseUpdateRequestDto {

    @NotNull
    private LocalDate startDate;

    // Peab olema startDate või hilisem (kontrollib CourseService → 403 COURSE_END_BEFORE_START)
    @NotNull
    private LocalDate endDate;

    // Frontend pakub tööpäevade arvu, backend ei arvuta
    @NotNull
    @Min(1)
    private Integer numberOfDays;

    @NotNull
    @Min(1)
    private Integer numberOfAcademicHours;

    @NotNull
    @DecimalMin("0")
    private BigDecimal price;

    // Koolitajad järjekorras (sort_order = indeks + 1); tühi list = koolitajaid pole
    @NotNull
    @UniqueElements(message = "ei tohi sisaldada korduvaid väärtusi")
    private List<@NotNull Integer> lecturerIds;

    // null = ruum puudub
    private Integer roomId;

    // U / O / F / X — kustutamiseks (D) on DELETE /api/course/{courseId}
    @NotNull
    @Pattern(regexp = "[UOFX]", message = "peab olema U, O, F või X")
    private String status;

    // Tühi → null
    private String notes;

    // Tühi → null
    @Size(max = 255)
    private String meetingLink;
}
