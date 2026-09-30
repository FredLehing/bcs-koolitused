package ee.bcskoolitus.persistance.lecturer;

import ee.bcskoolitus.controller.common.dto.LecturerDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LecturerMapper {

    @Mapping(source = "id", target = "lecturerId")
    @Mapping(source = "fullName", target = "lecturerName")
    LecturerDto toLecturerDto(Lecturer lecturer);

    List<LecturerDto> toLecturerDtos(List<Lecturer> lecturers);

}
