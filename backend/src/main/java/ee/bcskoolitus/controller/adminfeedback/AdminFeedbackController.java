package ee.bcskoolitus.controller.adminfeedback;

import ee.bcskoolitus.controller.adminfeedback.dto.*;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.infrastructure.exception.IncorrectInputException;
import ee.bcskoolitus.service.AdminFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "INCORRECT_INPUT: väljanimi: vigane väärtus", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "PRIMARY_KEY_NOT_FOUND: ID puudub", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Ootamatu serveriviga")
})
public class AdminFeedbackController {
    private final AdminFeedbackService adminFeedbackService;

    @GetMapping("/admin-feedback-courses")
    @Operation(summary = "Admini tagasiside filtri toimunud toimumiskorrad", description = "Lõppkuupäev enne tänast, staatus O/F, ka tagasisideta. Pealkiri contentLang keeles, puudumisel põhikeeles. Järjestus endDate DESC, courseId DESC.")
    public List<AdminFeedbackCourseDto> getAdminFeedbackCourses(@RequestParam(required = false) String contentLang) {
        return adminFeedbackService.getAdminFeedbackCourses(contentLang);
    }

    @GetMapping("/admin-feedbacks")
    @Operation(summary = "Admini tagasisided koos koondandmetega", description = "Üks rida feedback kohta. Kõik koondid kogu filtreeritud hulgast, mitte lehest. Vaikimisi N/U enne H, grupis createdAt DESC. Vastamismäär loeb R-registreerumisi ja sõltub vaid courseId-st. Ajalooline soft delete ei peida vastuseid.")
    public AdminFeedbackPageDto getAdminFeedbackPage(@ParameterObject @ModelAttribute AdminFeedbackFilterDto adminFeedbackFilterDto) {
        return adminFeedbackService.getAdminFeedbackPage(adminFeedbackFilterDto);
    }

    @GetMapping("/admin-feedback/{feedbackId}")
    @Operation(summary = "Admini avatud tagasiside vastused", description = "Ainult antud vastused, ka ajaloolised kriteeriumid. Lugemine ei muuda staatust. answersVersion on läbipaistmatu versioon järgmisele review-kutsele.")
    public AdminFeedbackDto getAdminFeedback(@PathVariable String feedbackId, @RequestParam(required = false) String contentLang) {
        return adminFeedbackService.getAdminFeedback(positiveFeedbackId(feedbackId), contentLang);
    }

    @PutMapping("/admin-feedback/{feedbackId}/review")
    @Operation(summary = "Admin märgib tagasiside üle vaadatuks", description = "N/U → H. Body puudub. Sama versiooniga H on idempotentne. Muudetud vastused → 409, vastuseid ei muudeta. Ühine lukk osaleja PUT-iga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Üle vaadatud; tühi vastus", content = @Content),
            @ApiResponse(responseCode = "409", description = "FEEDBACK_ANSWERS_CHANGED: Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void reviewAdminFeedback(@PathVariable String feedbackId,
                                   @Parameter(required = true, description = "Avatud detaili answersVersion")
                                   @RequestHeader(value = "X-Answers-Version", required = false) String answersVersion) {
        adminFeedbackService.reviewAdminFeedback(positiveFeedbackId(feedbackId), answersVersion);
    }

    private static Integer positiveFeedbackId(String value) {
        Integer feedbackId = AdminFeedbackFilterDto.integerValue("feedbackId", value, null);
        if (feedbackId == null || feedbackId <= 0) throw new IncorrectInputException("feedbackId");
        return feedbackId;
    }
}
