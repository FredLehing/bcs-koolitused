package ee.bcskoolitus.controller.lecturertranslation;

import ee.bcskoolitus.controller.lecturertranslation.dto.LecturerTranslationDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.LecturerTranslationService;
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
@RequiredArgsConstructor
@RequestMapping("/api")
public class LecturerTranslationController {

    private final LecturerTranslationService lecturerTranslationService;

    @GetMapping("/lecturer-translation/{lecturerTranslationId}")
    @Operation(summary = "Tagastab koolitaja ühe tõlke andmed ID järgi",
            description = "title = ametinimetus; description on richtext (HTML).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu lecturerTranslationId või kustutatud koolitaja tõlge -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerTranslationDto getLecturerTranslation(@PathVariable Integer lecturerTranslationId) {
        return lecturerTranslationService.getLecturerTranslation(lecturerTranslationId);
    }
}
