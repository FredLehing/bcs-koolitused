package ee.bcskoolitus.controller.trainingtranslation;

import ee.bcskoolitus.controller.trainingtranslation.dto.TrainingTranslationDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculum;
import ee.bcskoolitus.service.TrainingTranslationCurriculumService;
import ee.bcskoolitus.service.TrainingTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingTranslationController {

    private final TrainingTranslationService trainingTranslationService;
    private final TrainingTranslationCurriculumService trainingTranslationCurriculumService;

    @GetMapping("/training-translation/{trainingTranslationId}")
    @Operation(summary = "Tagastab koolituse ühe tõlke andmed ID järgi",
            description = "description on richtext (HTML) ja tagastatakse muutmata kujul. curriculumFileName ja curriculumFileSize (baitides) = õppekava, null = õppekava pole; faili baite ei loeta.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingTranslationId või kustutatud koolitus -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingTranslationDto getTrainingTranslation(@PathVariable Integer trainingTranslationId) {
        return trainingTranslationService.getTrainingTranslation(trainingTranslationId);
    }

    @GetMapping("/training-translation/{trainingTranslationId}/curriculum")
    @Operation(summary = "Laadib alla tõlke õppekava (PDF)",
            description = "Content-Disposition: attachment, failinimi = training_translation_curriculum.file_name. Cache-Control: no-cache, sest fail võib sama URL-i all vahetuda. Kättesaadav mustandi (U) ja publitseeritud (P) koolitusel — piirang lisatakse koos autentimisega.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingTranslationId, kustutatud koolitus või tõlkel pole õppekava -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<byte[]> getTrainingTranslationCurriculum(@PathVariable Integer trainingTranslationId) {
        trainingTranslationService.getValidActiveTrainingTranslationBy(trainingTranslationId);
        TrainingTranslationCurriculum trainingTranslationCurriculum =
                trainingTranslationCurriculumService.getValidTrainingTranslationCurriculumBy(trainingTranslationId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(trainingTranslationCurriculum.getFileName())
                        .build()
                        .toString())
                .cacheControl(CacheControl.noCache())
                .body(trainingTranslationCurriculum.getFile());
    }
}
