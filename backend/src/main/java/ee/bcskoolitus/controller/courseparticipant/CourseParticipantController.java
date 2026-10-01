package ee.bcskoolitus.controller.courseparticipant;

import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantStatusDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
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
}
