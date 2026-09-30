package ee.bcskoolitus.persistance.view.coursesummary;

import ee.bcskoolitus.controller.course.dto.CourseSummaryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseSummaryMapper {

    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "numberOfAcademicHours", target = "numberOfAcademicHours")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "lecturerNames", target = "lecturerNames")
    @Mapping(source = "roomName", target = "roomName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isPast", target = "isPast")
    @Mapping(source = "participantCount", target = "participantCount")
    @Mapping(source = "hasNotes", target = "hasNotes")
    @Mapping(source = "hasMeetingLink", target = "hasMeetingLink")
    CourseSummaryDto toCourseSummaryDto(CourseSummary courseSummary);

    List<CourseSummaryDto> toCourseSummaryDtos(List<CourseSummary> courseSummaries);
}
