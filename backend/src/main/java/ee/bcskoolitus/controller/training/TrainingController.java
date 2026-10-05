package ee.bcskoolitus.controller.training;

import ee.bcskoolitus.controller.training.dto.AdminTrainingDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingFilterDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingTitleDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
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

    @GetMapping("/admin-trainings")
    @Operation(summary = "Tagastab admini koolituste tabeli (filtrid, sorteerimine, leheküljestus)",
            description = "status puudub = aktiivsed (U ja P). searchText otsib ainult pealkirjast (iga sõna peab esinema). "
                    + "sortBy: createdAt / updatedAt / title / categoryName / trainingLanguageCode / status / hasAllTranslations (tundmatu → createdAt), "
                    + "sortDirection: asc / desc. Puuduva contentLang tõlke korral põhikeele pealkiri ja kategooria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik parameeter puudub või on vigane -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminTrainingSummaryDto findAdminTrainings(@Valid @ParameterObject AdminTrainingFilterDto adminTrainingFilterDto) {
        return trainingService.findAdminTrainings(adminTrainingFilterDto);
    }

    @GetMapping("/training-titles")
    @Operation(summary = "Tagastab aktiivsete koolituste nimed otsingu ettepanekuteks",
            description = "Koolitused staatusega U ja P, nimi contentLang keeles (puuduva tõlke korral põhikeeles), sorteeritud nime järgi.")
    public List<TrainingTitleDto> getTrainingTitles(@RequestParam String contentLang) {
        return trainingService.getTrainingTitles(contentLang);
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

    @GetMapping("/admin-training/{trainingId}")
    @Operation(summary = "Koolituse admini ülevaade (kalender, toimumiskorra vorm)",
            description = "Ka mustand. title, description, categoryName ja trainingTranslationId contentLang keeles, puudumisel põhikeeles. lecturers = training_lecturer sort_order järjekorras. Pilte ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminTrainingDto getAdminTraining(@PathVariable Integer trainingId, @RequestParam String contentLang) {
        return trainingService.getAdminTraining(trainingId, contentLang);
    }

    @PostMapping("/training")
    @Operation(summary = "Lisab uue koolituse koos põhikeele tõlkega",
            description = "Loob training rea (status U = mustand), training_funding_type read, training_lecturer read (lecturerIds järjekorras) põhikeele (language.is_main_language) tõlke ja õppekava (curriculum, valikuline PDF Base64-na) ühes transaktsioonis. Õppekava failinimi = pealkiri + curriculumLabel. Tagastab uue koolituse ja tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu categoryId / locationId / trainingLanguageId / fundingTypeId / userId, olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "curriculum ei ole PDF -> 'errorCode:' CURRICULUM_TYPE_NOT_ALLOWED, curriculum on üle 10 MB -> 'errorCode:' CURRICULUM_TOO_LARGE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli (ka curriculumLabel) puudub, on liiga pikk, curriculum on vigane Base64 või lecturerIds sisaldab korduvat ID-d -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingCreateResponseDto addTraining(@Valid @RequestBody TrainingCreateRequestDto trainingCreateRequestDto) {
        return trainingService.addTraining(trainingCreateRequestDto);
    }

    @PutMapping("/training/{trainingId}")
    @Operation(summary = "Muudab koolituse andmeid ja avatud tõlke tekste",
            description = "Uuendab training rea, kirjutab training_funding_type read fundingTypeIds järgi ja training_lecturer read lecturerIds järgi üle ning uuendab trainingTranslationId tõlke tekstid ja õppekava ühes transaktsioonis: curriculum = uus fail, isCurriculumRemoved = true eemaldab, muidu arvutatakse olemasoleva faili nimi uuesti (pealkiri + curriculumLabel). Koolituse autor ja staatus ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId / trainingTranslationId (ka teisele koolitusele kuuluv) / categoryId / trainingLanguageId / locationId / fundingTypeId, olematu lecturerId või uus kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "curriculum ei ole PDF -> 'errorCode:' CURRICULUM_TYPE_NOT_ALLOWED, curriculum on üle 10 MB -> 'errorCode:' CURRICULUM_TOO_LARGE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli (ka curriculumLabel) puudub, on liiga pikk, kirjeldus on tühi, curriculum on vigane Base64 või antud koos isCurriculumRemoved = true, lecturerIds sisaldab korduvat ID-d -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateTraining(@PathVariable Integer trainingId,
                               @Valid @RequestBody TrainingUpdateRequestDto trainingUpdateRequestDto) {
        trainingService.updateTraining(trainingId, trainingUpdateRequestDto);
    }

    @PostMapping("/training/{trainingId}/training-translation")
    @Operation(summary = "Lisab koolitusele tõlke uude keelde",
            description = "Loob ühe training_translation rea ja õppekava (curriculum, valikuline PDF Base64-na; failinimi = pealkiri + curriculumLabel). Koolituse andmeid ega staatust ei muudeta. Tagastab uue tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu trainingId / languageId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitusel on selles keeles tõlge juba olemas -> 'errorCode:' TRANSLATION_EXISTS; curriculum ei ole PDF -> CURRICULUM_TYPE_NOT_ALLOWED; curriculum on üle 10 MB -> CURRICULUM_TOO_LARGE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli (ka curriculumLabel) puudub, on liiga pikk, kirjeldus on tühi või curriculum on vigane Base64 -> 'errorCode:' INCORRECT_INPUT",
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
