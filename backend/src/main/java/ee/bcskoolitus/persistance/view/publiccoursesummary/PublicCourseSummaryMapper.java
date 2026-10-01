package ee.bcskoolitus.persistance.view.publiccoursesummary;

import ee.bcskoolitus.controller.course.dto.PublicCourseSummaryItemDto;
import ee.bcskoolitus.controller.common.dto.UpcomingCourseDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PublicCourseSummaryMapper {

    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "trainingId", target = "trainingId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "trainingLanguageFlagIconCode", target = "trainingLanguageFlagIconCode")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "numberOfDays", target = "numberOfDays")
    @Mapping(source = "numberOfAcademicHours", target = "numberOfAcademicHours")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isPromoted", target = "isPromoted")
    @Mapping(source = "isOnSite", target = "isOnSite")
    @Mapping(source = "isOnline", target = "isOnline")
    @Mapping(source = "lecturerNames", target = "lecturerNames")
    @Mapping(target = "fundingTypes", ignore = true)
    PublicCourseSummaryItemDto toPublicCourseSummaryItemDto(PublicCourseSummary publicCourseSummary);

    List<PublicCourseSummaryItemDto> toPublicCourseSummaryItemDtos(List<PublicCourseSummary> publicCourseSummaries);

    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isOnSite", target = "isOnSite")
    @Mapping(source = "isOnline", target = "isOnline")
    UpcomingCourseDto toUpcomingCourseDto(PublicCourseSummary publicCourseSummary);

    List<UpcomingCourseDto> toUpcomingCourseDtos(List<PublicCourseSummary> publicCourseSummaries);
}
