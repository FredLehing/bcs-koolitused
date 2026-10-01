package ee.bcskoolitus.controller.lecturer;

import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.lecturer.dto.AdminLecturerSummaryDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateResponseDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerDetailDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerProfileDto;
import ee.bcskoolitus.controller.common.dto.LecturerSummaryDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateResponseDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationItemDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerUpdateRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.persistance.lecturer.photo.LecturerPhoto;
import ee.bcskoolitus.service.LecturerPhotoService;
import ee.bcskoolitus.service.LecturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LecturerController {
    private final LecturerService lecturerService;
    private final LecturerPhotoService lecturerPhotoService;

    @GetMapping("/lecturers")
    @Operation(summary = "Otsib aktiivseid koolitajaid nime järgi",
            description = "Tagastab aktiivsed (status A) koolitajad, kelle nimi sisaldab otsingusõna (tõstutundetu), nime järgi tähestikuliselt. Tühi otsingusõna tagastab kõik aktiivsed koolitajad. Kustutatud koolitajaid ei tagastata. Kasutatakse TrainingFormView \"Vali lektor\" modalis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<LecturerDto> findLecturers(@RequestParam(required = false, defaultValue = "") String search) {
        return lecturerService.findLecturers(search);
    }

    @GetMapping("/admin-lecturers")
    @Operation(summary = "Admini koolitajate nimekiri",
            description = "Nime järgi sorteeritud koolitajad (vaikimisi ainult aktiivsed; includeDeleted=true → ka kustutatud). title ja lecturerTranslationId on contentLang keeles, puudumisel põhikeeles. Pilte ei tagastata. Tundmatu contentLang → tühi list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<AdminLecturerSummaryDto> findAdminLecturers(@RequestParam String contentLang,
                                                            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        return lecturerService.findAdminLecturers(contentLang, includeDeleted);
    }

    @GetMapping("/lecturer/{lecturerId}")
    @Operation(summary = "Tagastab koolitaja nime ja pildi versiooni (vorm)",
            description = "photoVersion = lecturer_photo.updated_at epoch-sekundites või null, kui pilti pole. Pilt ise: GET /api/lecturer/{lecturerId}/photo?v={photoVersion}. Tõlkeid ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerDetailDto getLecturer(@PathVariable Integer lecturerId) {
        return lecturerService.getLecturer(lecturerId);
    }

    @GetMapping("/lecturer/{lecturerId}/photo")
    @Operation(summary = "Tagastab koolitaja pildi baidid (<img src>)",
            description = "Avalik. Content-Type = lecturer_photo.content_type (normaliseeritud pildil image/jpeg). Query parameeter v (photoVersion) on ainult vahemälu jaoks — URL muutub pildi vahetamisel, seega vahemälu kehtib aasta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId või koolitajal pole pilti -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<byte[]> getLecturerPhoto(@PathVariable Integer lecturerId,
                                                   @RequestParam(required = false) Long v) {
        lecturerService.getValidActiveLecturerBy(lecturerId, "lecturerId");
        LecturerPhoto lecturerPhoto = lecturerPhotoService.getValidLecturerPhotoBy(lecturerId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(lecturerPhoto.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(lecturerPhoto.getPhoto());
    }

    @GetMapping("/lecturer/{lecturerId}/lecturer-translations")
    @Operation(summary = "Tagastab koolitaja olemasolevad tõlked (lipukesed)",
            description = "Sorteeritud language.id järgi (põhikeel esimesena).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<LecturerTranslationItemDto> getLecturerTranslations(@PathVariable Integer lecturerId) {
        return lecturerService.getLecturerTranslations(lecturerId);
    }

    @PostMapping("/lecturer")
    @Operation(summary = "Lisab uue koolitaja koos põhikeele tõlkega",
            description = "Loob lecturer rea (status A), pildi korral lecturer_photo rea (pilt normaliseeritakse: ruut 400×400, JPEG) ja põhikeele tõlke ühes transaktsioonis. Tagastab uue koolitaja ja tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Pildi tüüp pole PNG/JPEG/WebP -> 'errorCode:' PHOTO_TYPE_NOT_ALLOWED; pilt üle 2 MB -> 'errorCode:' PHOTO_TOO_LARGE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, on liiga pikk, kirjeldus on tühi või photo pole korrektne Base64 -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerCreateResponseDto addLecturer(@Valid @RequestBody LecturerCreateRequestDto lecturerCreateRequestDto) {
        return lecturerService.addLecturer(lecturerCreateRequestDto);
    }

    @PutMapping("/lecturer/{lecturerId}")
    @Operation(summary = "Muudab koolitaja nime, pilti ja avatud tõlget",
            description = "Ühes transaktsioonis. Pilt: photo ≠ null → uus pilt (normaliseeritakse), lisatakse või asendatakse; photo = null → pilti ei muudeta; isPhotoRemoved = true → pilt eemaldatakse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId / olematu või teisele koolitajale kuuluv lecturerTranslationId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Pildi tüüp pole PNG/JPEG/WebP -> 'errorCode:' PHOTO_TYPE_NOT_ALLOWED; pilt üle 2 MB -> 'errorCode:' PHOTO_TOO_LARGE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, on liiga pikk, kirjeldus on tühi, photo pole korrektne Base64 või isPhotoRemoved koos photo'ga -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateLecturer(@PathVariable Integer lecturerId,
                               @Valid @RequestBody LecturerUpdateRequestDto lecturerUpdateRequestDto) {
        lecturerService.updateLecturer(lecturerId, lecturerUpdateRequestDto);
    }

    @PostMapping("/lecturer/{lecturerId}/lecturer-translation")
    @Operation(summary = "Lisab koolitajale tõlke uude keelde",
            description = "Loob ühe lecturer_translation rea (description puhastatakse). Tagastab uue tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId / olematu languageId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitajal on selles keeles tõlge juba olemas -> 'errorCode:' TRANSLATION_EXISTS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, on liiga pikk või kirjeldus on tühi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerTranslationCreateResponseDto addLecturerTranslation(@PathVariable Integer lecturerId,
                                                                       @Valid @RequestBody LecturerTranslationCreateRequestDto lecturerTranslationCreateRequestDto) {
        return lecturerService.addLecturerTranslation(lecturerId, lecturerTranslationCreateRequestDto);
    }

    @DeleteMapping("/lecturer/{lecturerId}")
    @Operation(summary = "Kustutab koolitaja (soft delete)",
            description = "Määrab lecturer.status = D. Tõlkeid, pilti ega seoseid ei kustutata. Juba kustutatud koolitaja korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Koolitajal on tulevasi toimumiskordi -> 'errorCode:' LECTURER_HAS_UPCOMING_COURSES",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deleteLecturer(@PathVariable Integer lecturerId) {
        lecturerService.deleteLecturer(lecturerId);
    }

    @PutMapping("/lecturer/{lecturerId}/restore")
    @Operation(summary = "Taastab kustutatud koolitaja",
            description = "Tegevusteenus: määrab kustutatud koolitaja (status D) staatuseks A. Aktiivse koolitaja korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void restoreLecturer(@PathVariable Integer lecturerId) {
        lecturerService.restoreLecturer(lecturerId);
    }

    @GetMapping("/lecturer-summary/{lecturerId}")
    @Operation(summary = "Koolitaja kaardi andmed (LecturerCard)",
            description = "title ja shortDescription contentLang keeles, puudumisel põhikeeles. Pildi asemel photoVersion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerSummaryDto getLecturerSummary(@PathVariable Integer lecturerId, @RequestParam String contentLang) {
        return lecturerService.getLecturerSummary(lecturerId, contentLang);
    }

    @GetMapping("/lecturer-summaries")
    @Operation(summary = "Avalik koolitajate nimekiri (\"Meie koolitajad\")",
            description = "Aktiivsed koolitajad nime järgi. title ja shortDescription contentLang keeles, puudumisel põhikeeles. Pildi asemel photoVersion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<LecturerSummaryDto> findLecturerSummaries(@RequestParam String contentLang) {
        return lecturerService.findLecturerSummaries(contentLang);
    }

    @GetMapping("/lecturer-profile/{lecturerId}")
    @Operation(summary = "Koolitaja avalik profiil",
            description = "Tekstid contentLang keeles, puudumisel põhikeeles; description puhastatud HTML. trainings = publitseeritud koolitused, kus ta on koolitaja, pealkirja järgi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LecturerProfileDto getLecturerProfile(@PathVariable Integer lecturerId, @RequestParam String contentLang) {
        return lecturerService.getLecturerProfile(lecturerId, contentLang);
    }
}
