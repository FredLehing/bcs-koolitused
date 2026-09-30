package ee.bcskoolitus.controller.training;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingService;
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
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping("/trainings")
    @Operation(summary = "Tagastab koolituste nimekirja koos leheküljestamiseks vajaliku metainfoga",
            description = "searchText: tühikutega eraldatud sõnad, millest iga peab esinema (contains, tõstutundetu) koolituse pealkirjas või lühikirjelduses. Tühi string = otsingut ei rakendata.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Integer väljale lisatakse String, mis põhjustab veateate",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingSummaryDto findFilteredTrainings(@RequestParam Integer categoryId,
                                                    @RequestParam Integer fundingTypeId,
                                                    @RequestParam Integer limit,
                                                    @RequestParam Integer page,
                                                    @RequestParam Integer trainingLanguageId,
                                                    @RequestParam String contentLang,
                                                    @RequestParam String searchText) {
        return trainingService.findFilteredTrainings(categoryId, fundingTypeId, limit, page, trainingLanguageId, contentLang, searchText);
    }

    @GetMapping("/training/{trainingId}")
    @Operation(summary = "Tagastab koolituse põhiandmed ID järgi")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingDto getTraining(@PathVariable Integer trainingId) {
        return trainingService.getTraining(trainingId);
    }

    @GetMapping("/training/{trainingId}/training-translations")
    @Operation(summary = "Tagastab koolituse olemasolevad keeletõlked")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<TrainingTranslationItemDto> getTrainingTranslations(
            @PathVariable Integer trainingId) {

        return trainingService.getTrainingTranslations(trainingId);
    }

    @PostMapping("/training")
    @Operation(summary = "Lisab uue koolituse koos põhikeele tõlkega",
            description = "Loob training rea (status U = mustand), training_funding_type read ja põhikeele (language.is_main_language) tõlke ühes transaktsioonis. Tagastab uue koolituse ja tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu categoryId / locationId / trainingLanguageId / defaultLecturerId / fundingTypeId / userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub või on liiga pikk -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingCreateResponseDto addTraining(@Valid @RequestBody TrainingCreateRequestDto trainingCreateRequestDto) {
        return trainingService.addTraining(trainingCreateRequestDto);
    }

    @PutMapping("/training/{trainingId}")
    @Operation(summary = "Muudab koolituse andmeid ja avatud tõlke tekste",
            description = "Uuendab training rea, kirjutab training_funding_type read fundingTypeIds järgi üle ja uuendab trainingTranslationId tõlke tekstid ühes transaktsioonis. Koolituse autor ja staatus ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId / trainingTranslationId (ka teisele koolitusele kuuluv) / categoryId / trainingLanguageId / locationId / defaultLecturerId / fundingTypeId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, on liiga pikk või kirjeldus on tühi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateTraining(@PathVariable Integer trainingId,
                               @Valid @RequestBody TrainingUpdateRequestDto trainingUpdateRequestDto) {
        trainingService.updateTraining(trainingId, trainingUpdateRequestDto);
    }

    @PostMapping("/training/{trainingId}/training-translation")
    @Operation(summary = "Lisab koolitusele tõlke uude keelde",
            description = "Loob ühe training_translation rea. Koolituse andmeid ega staatust ei muudeta. Tagastab uue tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId / languageId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitusel on selles keeles tõlge juba olemas -> 'errorCode:' TRANSLATION_EXISTS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, on liiga pikk või kirjeldus on tühi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingTranslationCreateResponseDto addTrainingTranslation(@PathVariable Integer trainingId,
                                                                       @Valid @RequestBody TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto) {
        return trainingService.addTrainingTranslation(trainingId, trainingTranslationCreateRequestDto);
    }

    @DeleteMapping("/training/{trainingId}")
    @Operation(summary = "Kustutab koolituse (soft delete)",
            description = "Määrab training.status = D. Tõlkeid ega rahastustüüpe ei kustutata. Juba kustutatud koolituse korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deleteTraining(@PathVariable Integer trainingId) {
        trainingService.deleteTraining(trainingId);
    }

    @PutMapping("/training/{trainingId}/restore")
    @Operation(summary = "Taastab kustutatud koolituse mustandisse",
            description = "Tegevusteenus: määrab kustutatud koolituse (status D) staatuseks U. Kustutamata koolituse korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void restoreTraining(@PathVariable Integer trainingId) {
        trainingService.restoreTraining(trainingId);
    }

    @PutMapping("/training/{trainingId}/publish")
    @Operation(summary = "Publitseerib koolituse",
            description = "Tegevusteenus: määrab status = P. Juba publitseeritud koolituse korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitus on kustutatud (status D) -> 'errorCode:' TRAINING_DELETED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void publishTraining(@PathVariable Integer trainingId) {
        trainingService.publishTraining(trainingId);
    }

    @PutMapping("/training/{trainingId}/unpublish")
    @Operation(summary = "Liigutab koolituse mustandisse",
            description = "Tegevusteenus: määrab status = U. Juba mustandis koolituse korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitus on kustutatud (status D) -> 'errorCode:' TRAINING_DELETED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void unpublishTraining(@PathVariable Integer trainingId) {
        trainingService.unpublishTraining(trainingId);
    }
}
