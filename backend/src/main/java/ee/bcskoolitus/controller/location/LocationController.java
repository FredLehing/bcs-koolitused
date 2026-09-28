package ee.bcskoolitus.controller.location;

import ee.bcskoolitus.controller.location.dto.LocationDto;
import ee.bcskoolitus.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/locations")
    @Operation(summary = "Tagastab kõik toimumiskohad",
            description = "Toimumiskohtade nimekiri (id järgi kasvavalt) TrainingFormView \"Toimumiskoht\" rippmenüü jaoks. location.name ei ole tõlgitud, seega contentLang parameetrit pole.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<LocationDto> findAllLocations() {
        return locationService.findAllLocations();
    }
}
