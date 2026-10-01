package ee.bcskoolitus.controller.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCourseDto {
    private Integer courseId;
    private Integer trainingId;
    // contentLang keele tõlge, puudumisel põhikeele tõlge — link koolituse lehele avab selle
    private Integer trainingTranslationId;
    private String trainingTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPast;
    private Integer numberOfDays;
    private Integer numberOfAcademicHours;
    private BigDecimal price;
    private String status;
    private Boolean isPromoted;
    // Koolitajad sort_order järjekorras, nt "Rain Tüür, Meelis Teern"; null = koolitajaid pole
    private String lecturerNames;
    // Ka kustutatud ruumi nimi; null = ruum puudub
    private String roomName;
    private String meetingLink;
    private String notes;
}
