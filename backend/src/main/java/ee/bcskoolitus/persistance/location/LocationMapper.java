package ee.bcskoolitus.persistance.location;

import ee.bcskoolitus.controller.location.dto.LocationDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {

    @Mapping(source = "id", target = "locationId")
    @Mapping(source = "name", target = "locationName")
    LocationDto toLocationDto(Location location);

    List<LocationDto> toLocationDtos(List<Location> locations);

}
