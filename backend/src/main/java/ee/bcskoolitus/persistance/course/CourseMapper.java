package ee.bcskoolitus.persistance.course;

import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CoursePageDto;
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
    @Mapping(source = "isPromoted", target = "isPromoted")
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
    @Mapping(source = "isPromoted", target = "isPromoted")
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

    // Tekstid, rahastus, koolitajad ja teised toimumiskorrad määrab CourseService; veebilinki ei tagastata
    @Mapping(source = "id", target = "courseId")
    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "training.trainingLanguage.flagIconCode", target = "trainingLanguageFlagIconCode")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(expression = "java(course.getEndDate().isBefore(java.time.LocalDate.now()))", target = "isPast")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "numberOfAcademicHours", target = "numberOfAcademicHours")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(expression = "java(course.getRoom() != null)", target = "isOnSite")
    @Mapping(expression = "java(course.getMeetingLink() != null && !course.getMeetingLink().isBlank())", target = "isOnline")
    @Mapping(target = "trainingTranslationId", ignore = true)
    @Mapping(target = "isMainLanguageFallback", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "shortDescription", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "fundingTypes", ignore = true)
    @Mapping(target = "lecturers", ignore = true)
    @Mapping(target = "upcomingCourses", ignore = true)
    CoursePageDto toCoursePageDto(Course course);
}
