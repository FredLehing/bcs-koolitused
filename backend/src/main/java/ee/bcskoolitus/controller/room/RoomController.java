package ee.bcskoolitus.controller.room;

import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.service.RoomService;
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
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/rooms")
    @Operation(summary = "Tagastab kõik ruumid nime järgi",
            description = "CourseFormView ruumi rippmenüü. roomStatus (VAB / KIN) tähendus on lahtine — praegu tagastatakse kõik ruumid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<RoomDto> findRooms() {
        return roomService.findRooms();
    }
}
