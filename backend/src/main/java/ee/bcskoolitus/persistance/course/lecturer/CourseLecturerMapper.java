package ee.bcskoolitus.persistance.course.lecturer;

import ee.bcskoolitus.controller.common.dto.LecturerDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseLecturerMapper {

    @Mapping(source = "lecturer.id", target = "lecturerId")
    @Mapping(source = "lecturer.fullName", target = "lecturerName")
    LecturerDto toLecturerDto(CourseLecturer courseLecturer);

    List<LecturerDto> toLecturerDtos(List<CourseLecturer> courseLecturers);
}
