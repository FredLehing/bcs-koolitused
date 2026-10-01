package ee.bcskoolitus.controller.courseparticipant;

import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationSummaryDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationUpdateRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantStatusDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.MyRegistrationDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.CourseParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class CourseParticipantController {
    private final CourseParticipantService courseParticipantService;

    @GetMapping("/course/{courseId}/participants")
    @Operation(summary = "Toimumiskorra osalejad (admin)",
            description = "Registreerumise järjekorras (created_at), ka loobunud (status C). email ja phone osaleja profiilist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<CourseParticipantDto> findCourseParticipants(@PathVariable Integer courseId) {
        return courseParticipantService.findCourseParticipants(courseId);
    }

    @GetMapping("/admin-registrations")
    @Operation(summary = "Admini registreerumiste nimekiri",
            description = "Uusimad eespool (created_at kahanevalt). Vaikimisi ainult registreerunud (R) ja toimumiskorrad, mis pole lõppenud; "
                    + "includeCancelled=true → ka loobunud (C), includePast=true → ka toimunud. Kustutatud toimumiskorrad ja koolitused välja. "
                    + "trainingTitle contentLang keeles, puudumisel põhikeeles. Tundmatu contentLang → tühi list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<AdminRegistrationSummaryDto> findAdminRegistrations(@RequestParam String contentLang,
                                                                    @RequestParam(required = false, defaultValue = "false") Boolean includeCancelled,
                                                                    @RequestParam(required = false, defaultValue = "false") Boolean includePast) {
        return courseParticipantService.findAdminRegistrations(contentLang, includeCancelled, includePast);
    }

    @GetMapping("/admin-registration/{courseParticipantId}")
    @Operation(summary = "Ühe registreerumise admini vaade",
            description = "Registreerumise väljad, osaleja kontakt (profiil, accountEmail = kasutajakonto e-post) ja toimumiskord. "
                    + "notes = osaleja lisainfo (tühi string, kui puudub), adminNotes = admini märkmed (null, kui puudub). trainingTitle contentLang keeles, puudumisel põhikeeles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu courseParticipantId või tundmatu contentLang -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminRegistrationDto getAdminRegistration(@PathVariable Integer courseParticipantId, @RequestParam String contentLang) {
        return courseParticipantService.getAdminRegistration(courseParticipantId, contentLang);
    }

    @PutMapping("/admin-registration/{courseParticipantId}")
    @Operation(summary = "Admin muudab registreerumist",
            description = "Muutuvad ainult status (R/C), hasPaid, requiresLaptop ja adminNotes (trimmitakse, tühi → null); updated_at uueneb. "
                    + "Osaleja lisainfot (notes) ja profiili ei muudeta. Taastamine (C → R) on lubatud ka täis toimumiskorrale.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu courseParticipantId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub või status pole R/C -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateAdminRegistration(@PathVariable Integer courseParticipantId,
                                        @Valid @RequestBody AdminRegistrationUpdateRequestDto adminRegistrationUpdateRequestDto) {
        courseParticipantService.updateAdminRegistration(courseParticipantId, adminRegistrationUpdateRequestDto);
    }

    @GetMapping("/course/{courseId}/participant-status")
    @Operation(summary = "Sisselogitud kasutaja registreerumise olek toimumiskorral",
            description = "status: R = registreerunud, C = loobunud, null = pole registreerunud (ka siis, kui kasutajal osalejat pole).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu courseId / userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public CourseParticipantStatusDto getCourseParticipantStatus(@PathVariable Integer courseId, @RequestParam Integer userId) {
        return courseParticipantService.getCourseParticipantStatus(courseId, userId);
    }

    @PostMapping("/course/{courseId}/participant")
    @Operation(summary = "Kasutaja registreerib iseennast toimumiskorrale",
            description = "Ühes transaktsioonis: kasutaja osaleja (puudumisel luuakse profile + participant, olemasoleva profiil ja nimi uuenevad) ja "
                    + "course_participant (status R, has_paid = false); loobunu (C) rida muudetakse tagasi R-iks. requiresLaptop null → false, notes null → \"\".")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või mitteavalik courseId / olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Algus möödas -> REGISTRATION_CLOSED; juba registreerunud -> ALREADY_REGISTERED; toimumiskord täis -> COURSE_FULL",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub või vigane e-post -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void registerCourseParticipant(@PathVariable Integer courseId, @Valid @RequestBody CourseRegistrationRequestDto courseRegistrationRequestDto) {
        courseParticipantService.registerCourseParticipant(courseId, courseRegistrationRequestDto);
    }

    @GetMapping("/user/{userId}/registrations")
    @Operation(summary = "Kasutaja oma registreerumised (Minu koolitused)",
            description = "Ka loobunud (C) ja toimunud, alguse järgi kasvavalt; kustutatud toimumiskorrad välja. trainingTitle contentLang keeles, "
                    + "puudumisel põhikeeles. canCancel = R, toimumiskord pole alanud ega tühistatud. Osalejata kasutajal tühi list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<MyRegistrationDto> findMyRegistrations(@PathVariable Integer userId, @RequestParam String contentLang) {
        return courseParticipantService.findMyRegistrations(userId, contentLang);
    }

    @PutMapping("/user/{userId}/registration/{courseParticipantId}/cancel")
    @Operation(summary = "Kasutaja loobub oma registreerumisest",
            description = "status R → C, updated_at uueneb. Ainult enne toimumiskorra algust ja kui toimumiskord pole tühistatud.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId / courseParticipantId -> PRIMARY_KEY_NOT_FOUND; registreerumine pole selle kasutaja oma -> REGISTRATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Juba loobunud, toimumiskord alanud või tühistatud -> 'errorCode:' CANCEL_NOT_ALLOWED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void cancelMyRegistration(@PathVariable Integer userId, @PathVariable Integer courseParticipantId) {
        courseParticipantService.cancelMyRegistration(userId, courseParticipantId);
    }
}
