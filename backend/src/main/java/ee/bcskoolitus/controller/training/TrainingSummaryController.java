package ee.bcskoolitus.controller.training;

import ee.bcskoolitus.controller.training.dto.TrainingPageDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrainingSummaryController {
    private final TrainingPageService trainingPageService;

    @GetMapping("/training-summary/{trainingId}")
    @Operation(summary = "Tagastab koolituse detailvaate andmed",
            description = "Tõlke valik: sellele koolitusele kuuluv trainingTranslationId → contentLang → põhikeel. "
                    + "Õppekava nimi ja suurus pärinevad ainult valitud tõlkest; põhikeele varuteksti korral on need null. "
                    + "upcomingCourses sisaldab ainult publitseeritud koolituse avatud või täis (O/F) toimumiskordi alates tänasest, alguse ja ID järgi. "
                    + "Mustand (U) jääb vormi eelvaate jaoks avatavaks nagu olemasolev koolituse teenus; kustutatud koolitus (D) on 404. lecturers sisaldab aktiivsete koolitajate kaardiandmeid (fullName, title, shortDescription, photoVersion) kasutajaliidese contentLang keeles ja seoste järjekorras. Faili baite ei tagastata.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TrainingPageDto.class))),
            @ApiResponse(responseCode = "400", description = "Vigane ID parameetri tüüp", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Koolitus puudub, on kustutatud või sobivat tõlget pole", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public TrainingPageDto getTrainingPage(@PathVariable Integer trainingId,
            @Parameter(description = "Kuvatava sisu keel", example = "et") @RequestParam String contentLang,
            @Parameter(description = "Kindla tõlke eelvaade; võõras või olematu ID jäetakse valikus kõrvale")
            @RequestParam(required = false) Integer trainingTranslationId) {
        return trainingPageService.getTrainingPage(trainingId, contentLang, trainingTranslationId);
    }
}
