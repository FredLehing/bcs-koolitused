package ee.bcskoolitus.controller.feedback;

import ee.bcskoolitus.controller.feedback.dto.FeedbackRequestDto;
import ee.bcskoolitus.controller.feedback.dto.ParticipantFeedbackDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.FeedbackService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @GetMapping("/user/{userId}/registration/{courseParticipantId}/feedback")
    @Operation(summary = "Osaleja tagasiside vorm (Tagasiside)",
            description = "hasFeedback = false → criteria = aktiivsed kriteeriumid, score/feedbackText/createdAt/updatedAt null. "
                    + "hasFeedback = true → aktiivsed + kustutatud, millele on vastatud; hiljem lisatud kriteeriumi score null. "
                    + "Järjestus sequence, id. title/description ja trainingTitle contentLang keeles, puudumisel põhikeeles. "
                    + "updatedAt = vastuste viimane muutmise aeg.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId / courseParticipantId -> PRIMARY_KEY_NOT_FOUND; registreerumine pole selle kasutaja oma -> REGISTRATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Loobunud, toimumiskord pole lõppenud või on tühistatud/kustutatud -> 'errorCode:' FEEDBACK_NOT_ALLOWED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ParticipantFeedbackDto getParticipantFeedback(@PathVariable Integer userId, @PathVariable Integer courseParticipantId,
                                                         @RequestParam String contentLang) {
        return feedbackService.getParticipantFeedback(userId, courseParticipantId, contentLang);
    }

    @PostMapping("/user/{userId}/registration/{courseParticipantId}/feedback")
    @Operation(summary = "Osaleja lisab tagasiside",
            description = "Ühes transaktsioonis feedback (status N) ja vastus iga aktiivse kriteeriumi kohta. "
                    + "answers peab sisaldama täpselt kõiki aktiivseid kriteeriume. Tühi feedbackText → null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId / courseParticipantId -> PRIMARY_KEY_NOT_FOUND; registreerumine pole selle kasutaja oma -> REGISTRATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Tagasiside andmine pole lubatud -> FEEDBACK_NOT_ALLOWED; tagasiside juba olemas -> FEEDBACK_ALREADY_EXISTS; "
                            + "answers ei vasta kriteeriumidele -> FEEDBACK_CRITERIA_CHANGED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Hinne puudub või väljaspool 1–10, kommentaar üle 10 000 märgi, answers tühi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void addParticipantFeedback(@PathVariable Integer userId, @PathVariable Integer courseParticipantId,
                                       @Valid @RequestBody FeedbackRequestDto feedbackRequestDto) {
        feedbackService.addParticipantFeedback(userId, courseParticipantId, feedbackRequestDto);
    }

    @PutMapping("/user/{userId}/registration/{courseParticipantId}/feedback")
    @Operation(summary = "Osaleja muudab tagasisidet",
            description = "Vastused uuenevad, hiljem lisatud kriteeriumi vastus lisatakse. answers = aktiivsed + kustutatud, millele on vastatud. "
                    + "Staatus H → U, N ja U jäävad. Tühi feedbackText → null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId / courseParticipantId -> PRIMARY_KEY_NOT_FOUND; registreerumine pole selle kasutaja oma -> REGISTRATION_NOT_FOUND; "
                            + "tagasisidet pole -> FEEDBACK_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Tagasiside andmine pole lubatud -> FEEDBACK_NOT_ALLOWED; answers ei vasta kriteeriumidele -> FEEDBACK_CRITERIA_CHANGED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Hinne puudub või väljaspool 1–10, kommentaar üle 10 000 märgi, answers tühi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateParticipantFeedback(@PathVariable Integer userId, @PathVariable Integer courseParticipantId,
                                          @Valid @RequestBody FeedbackRequestDto feedbackRequestDto) {
        feedbackService.updateParticipantFeedback(userId, courseParticipantId, feedbackRequestDto);
    }
}
