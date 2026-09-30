package ee.bcskoolitus.persistance.training.lecturer;

import ee.bcskoolitus.controller.common.dto.LecturerDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingLecturerMapper {

    @Mapping(source = "lecturer.id", target = "lecturerId")
    @Mapping(source = "lecturer.fullName", target = "lecturerName")
    LecturerDto toLecturerDto(TrainingLecturer trainingLecturer);

    List<LecturerDto> toLecturerDtos(List<TrainingLecturer> trainingLecturers);
}
