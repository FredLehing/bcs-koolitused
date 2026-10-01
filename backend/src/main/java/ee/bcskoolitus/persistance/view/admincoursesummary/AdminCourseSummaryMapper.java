package ee.bcskoolitus.persistance.view.admincoursesummary;

import ee.bcskoolitus.controller.course.dto.AdminCourseDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryItemDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminCourseSummaryMapper {

    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "trainingId", target = "trainingId")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "isPast", target = "isPast")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isPromoted", target = "isPromoted")
    @Mapping(source = "hasMeetingLink", target = "hasMeetingLink")
    @Mapping(source = "participantCount", target = "participantCount")
    @Mapping(source = "paidCount", target = "paidCount")
    @Mapping(source = "enquiryCount", target = "enquiryCount")
    AdminCourseSummaryItemDto toAdminCourseSummaryItemDto(AdminCourseSummary adminCourseSummary);

    List<AdminCourseSummaryItemDto> toAdminCourseSummaryItemDtos(List<AdminCourseSummary> adminCourseSummaries);

    // Tunnid, koolitajad, ruum, veebilink ja märkmed määrab CourseService course tabelist
    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "trainingId", target = "trainingId")
    @Mapping(source = "trainingTranslationId", target = "trainingTranslationId")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "isPast", target = "isPast")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isPromoted", target = "isPromoted")
    @Mapping(target = "numberOfAcademicHours", ignore = true)
    @Mapping(target = "lecturerNames", ignore = true)
    @Mapping(target = "roomName", ignore = true)
    @Mapping(target = "meetingLink", ignore = true)
    @Mapping(target = "notes", ignore = true)
    AdminCourseDto toAdminCourseDto(AdminCourseSummary adminCourseSummary);
}
