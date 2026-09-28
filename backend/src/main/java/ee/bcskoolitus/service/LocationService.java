package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.location.dto.LocationDto;
import ee.bcskoolitus.persistance.location.Location;
import ee.bcskoolitus.persistance.location.LocationMapper;
import ee.bcskoolitus.persistance.location.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public List<LocationDto> findAllLocations() {
        List<Location> locations = locationRepository.findAllLocationsOrderedById();
        return locationMapper.toLocationDtos(locations);
    }
}
