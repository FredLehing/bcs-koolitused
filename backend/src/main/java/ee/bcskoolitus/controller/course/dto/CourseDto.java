package ee.bcskoolitus.controller.course.dto;

import ee.bcskoolitus.controller.common.dto.LecturerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {

    private Integer courseId;
    private Integer trainingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private Integer numberOfAcademicHours;
    private BigDecimal price;
    private List<LecturerDto> lecturers;
    private Integer roomId;
    // Ka kustutatud ruumi nimi — vorm näitab seda rippmenüüs "(kustutatud)"
    private String roomName;
    private String status;
    private String notes;
    private String meetingLink;
}
