package ee.bcskoolitus.persistance.view.adminroomsummary;

import ee.bcskoolitus.controller.room.dto.AdminRoomSummaryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminRoomSummaryMapper {

    @Mapping(source = "roomId", target = "roomId")
    @Mapping(source = "name", target = "roomName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "upcomingCourseCount", target = "upcomingCourseCount")
    @Mapping(source = "courseCount", target = "courseCount")
    @Mapping(source = "updatedAt", target = "updatedAt")
    AdminRoomSummaryDto toAdminRoomSummaryDto(AdminRoomSummary adminRoomSummary);

    List<AdminRoomSummaryDto> toAdminRoomSummaryDtos(List<AdminRoomSummary> adminRoomSummaries);
}
