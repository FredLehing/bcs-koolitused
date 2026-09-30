package ee.bcskoolitus.persistance.room;

import ee.bcskoolitus.controller.room.dto.RoomDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    @Mapping(source = "id", target = "roomId")
    @Mapping(source = "name", target = "roomName")
    RoomDto toRoomDto(Room room);

    List<RoomDto> toRoomDtos(List<Room> rooms);
}
