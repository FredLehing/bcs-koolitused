package ee.bcskoolitus.controller.room;

import ee.bcskoolitus.controller.room.dto.AdminRoomSummaryDto;
import ee.bcskoolitus.controller.room.dto.RoomCreateRequestDto;
import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.controller.room.dto.RoomUpdateRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/rooms")
    @Operation(summary = "Tagastab aktiivsed ruumid nime järgi",
            description = "CourseFormView ruumi rippmenüü. Kustutatud ruume (status D) ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<RoomDto> findRooms() {
        return roomService.findRooms();
    }

    @GetMapping("/admin-rooms")
    @Operation(summary = "Admini koolitusruumide nimekiri",
            description = "Nime järgi sorteeritud ruumid (vaikimisi ainult aktiivsed; includeDeleted=true → ka kustutatud). upcomingCourseCount = toimumiskorrad end_date ≥ täna, status ei ole D ega X; courseCount = kõik toimumiskorrad, status ≠ D.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<AdminRoomSummaryDto> findAdminRooms(@RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        return roomService.findAdminRooms(includeDeleted);
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Tagastab ruumi andmed (vorm)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud roomId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public RoomDto getRoom(@PathVariable Integer roomId) {
        return roomService.getRoom(roomId);
    }

    @PostMapping("/room")
    @Operation(summary = "Lisab uue ruumi",
            description = "Loob room rea (status A). Nimi peab olema unikaalne (tõstutundetult, ka kustutatud ruumide hulgas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sama nimega ruum on olemas -> 'errorCode:' ROOM_NAME_EXISTS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nimi puudub või on liiga pikk -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void addRoom(@Valid @RequestBody RoomCreateRequestDto roomCreateRequestDto) {
        roomService.addRoom(roomCreateRequestDto);
    }

    @PutMapping("/room/{roomId}")
    @Operation(summary = "Muudab ruumi nime",
            description = "Nimi peab olema unikaalne (tõstutundetult, see ruum välja arvatud).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud roomId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Teisel ruumil on sama nimi -> 'errorCode:' ROOM_NAME_EXISTS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nimi puudub või on liiga pikk -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateRoom(@PathVariable Integer roomId, @Valid @RequestBody RoomUpdateRequestDto roomUpdateRequestDto) {
        roomService.updateRoom(roomId, roomUpdateRequestDto);
    }

    @DeleteMapping("/room/{roomId}")
    @Operation(summary = "Kustutab ruumi (soft delete)",
            description = "Määrab room.status = D. Toimumiskordade seosed jäävad. Juba kustutatud ruumi korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu roomId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ruumis on tulevasi toimumiskordi -> 'errorCode:' ROOM_HAS_UPCOMING_COURSES",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deleteRoom(@PathVariable Integer roomId) {
        roomService.deleteRoom(roomId);
    }

    @PutMapping("/room/{roomId}/restore")
    @Operation(summary = "Taastab kustutatud ruumi",
            description = "Tegevusteenus: määrab kustutatud ruumi (status D) staatuseks A. Aktiivse ruumi korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu roomId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void restoreRoom(@PathVariable Integer roomId) {
        roomService.restoreRoom(roomId);
    }
}
