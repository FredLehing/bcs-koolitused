package ee.bcskoolitus.persistance.course;

import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CourseUpdateRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseMapper {

    // Seosed (koolitus, ruum, koolitajad, kasutaja), notes/meetingLink (tühi → null) ja ajatemplid määrab CourseService
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "numberOfAcademicHours", target = "numberOfAcademicHours")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "meetingLink", ignore = true)
    Course toCourse(CourseCreateRequestDto courseCreateRequestDto);

    // Seosed, notes/meetingLink määrab CourseService; koolitus, looja ja created_at ei muutu
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "numberOfAcademicHours", target = "numberOfAcademicHours")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "meetingLink", ignore = true)
    void updateCourse(CourseUpdateRequestDto courseUpdateRequestDto, @MappingTarget Course course);

    @Mapping(source = "id", target = "courseId")
    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(target = "lecturers", ignore = true)
    CourseDto toCourseDto(Course course);
}
