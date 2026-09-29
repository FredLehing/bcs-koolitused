package ee.bcskoolitus.controller.trainingtranslation;

import ee.bcskoolitus.controller.trainingtranslation.dto.TrainingTranslationDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingTranslationController {

    private final TrainingTranslationService trainingTranslationService;

    @GetMapping("/training-translation/{trainingTranslationId}")
    @Operation(summary = "Tagastab koolituse ühe tõlke andmed ID järgi",
            description = "description on richtext (HTML) ja tagastatakse muutmata kujul.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingTranslationId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingTranslationDto getTrainingTranslation(@PathVariable Integer trainingTranslationId) {
        return trainingTranslationService.getTrainingTranslation(trainingTranslationId);
    }
}
