package ee.bcskoolitus.controller.aitraining;

import ee.bcskoolitus.controller.common.dto.AiTrainingContentDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.AiTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai-training")
@Tag(name = "AI koolituse tekstid", description = "PDF-ist koostatud tekstid ja AI tõlked; tulemusi ei salvestata")
@RequiredArgsConstructor
public class AiTrainingController {

    private final AiTrainingService aiTrainingService;

    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    @Operation(summary = "loob PDF-ist koolituse väljade ettepaneku",
            description = "Kasutatakse olekutes new-training ja new-translation; sisend on vormis valitud salvestamata PDF. "
                    + "PDF saadetakse Gemini AI-le, mis kontrollib, kas tegu on õppekavaga, ning tagastab pealkirja, "
                    + "lühikirjelduse ja kirjelduse. Pealkiri ja lühikirjeldus lõigatakse 255 märgini, kirjelduse HTML puhastatakse. "
                    + "Vorm täidetakse vastusega, kuid andmebaasi midagi ei salvestata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF-ist loodud koolituse väljade ettepanek",
                    content = @Content(schema = @Schema(implementation = AiTrainingContentDto.class))),
            @ApiResponse(responseCode = "400", description = "Päringus puudub curriculum osa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Fail ei ole PDF (CURRICULUM_TYPE_NOT_ALLOWED), "
                    + "on üle 10 MB (CURRICULUM_TOO_LARGE) või ei ole õppekava (PDF_NOT_CURRICULUM)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "413", description = "Päring ületab multipart üleslaadimise piiri (10 MB)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "AI teenus pole saadaval (AI_SERVICE_UNAVAILABLE)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AiTrainingContentDto createTrainingContentFromPdf(
            @Parameter(description = "Salvestamata PDF-fail, millest koolituse väljad luuakse", required = true)
            @RequestPart("curriculum") MultipartFile curriculum) throws IOException {
        return aiTrainingService.createContentFromPdf(curriculum.getBytes());
    }

    @PostMapping("/pdf/{trainingTranslationId}")
    @Operation(summary = "loob tõlke salvestatud õppekavast koolituse väljade ettepaneku",
            description = "Kasutatakse olemasoleva tõlke muutmisel, kui vormis pole uut PDF-i valitud "
                    + "(uue faili korral kasutab frontend POST /api/ai-training/pdf). Loeb tõlke andmebaasi salvestatud õppekava "
                    + "ja saadab selle Gemini AI-le, mis kontrollib, kas tegu on õppekavaga, ning tagastab pealkirja, "
                    + "lühikirjelduse ja kirjelduse. Pealkiri ja lühikirjeldus lõigatakse 255 märgini, kirjelduse HTML puhastatakse. "
                    + "Vorm täidetakse vastusega, kuid andmebaasi midagi ei salvestata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Salvestatud õppekavast loodud koolituse väljade ettepanek",
                    content = @Content(schema = @Schema(implementation = AiTrainingContentDto.class))),
            @ApiResponse(responseCode = "403", description = "Salvestatud fail ei ole õppekava (PDF_NOT_CURRICULUM)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Tõlkel pole salvestatud õppekava",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "AI teenus pole saadaval (AI_SERVICE_UNAVAILABLE)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AiTrainingContentDto createTrainingContentFromSavedCurriculum(
            @Parameter(description = "Avatud tõlke ID", required = true) @PathVariable Integer trainingTranslationId) {
        return aiTrainingService.createContentFromSavedCurriculum(trainingTranslationId);
    }

    @PostMapping("/translation/{trainingId}")
    @Operation(summary = "TO BE IMPLEMENTED: tõlgib koolituse põhikeele tekstid AI abil",
            description = "Tulevane teostus loeb koolituse salvestatud põhikeele pealkirja, lühikirjelduse ja kirjelduse "
                    + "ning tõlgib need Gemini AI abil sihtkeelde. Vormi sisu ei saadeta ega kasutata. "
                    + "Kirjelduse HTML-vormindus säilitatakse. Tulemus täidab ainult vormi; andmebaasi midagi ei salvestata. "
                    + "Sobib nii uue tõlke lisamiseks kui olemasoleva tõlke muutmiseks. "
                    + "Praegu tagastab meetod fikseeritud placeholder-väärtused; andmebaasi ei loeta ega Gemini AI-d kutsuta. "
                    + "Dokumenteeritud ärivead lisatakse tegeliku teostusega. Vastusel on Cache-Control: no-store.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI tõlgitud koolituse tekstid (praegu placeholder)",
                    content = @Content(schema = @Schema(implementation = AiTrainingContentDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "AI-ga tõlgitud pealkiri (TO BE IMPLEMENTED)",
                                      "shortDescription": "AI-ga tõlgitud lühikirjeldus (TO BE IMPLEMENTED)",
                                      "description": "AI-ga tõlgitud kirjeldus (TO BE IMPLEMENTED)"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "languageId puudub või ID ei ole täisarv",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Põhikeelde ei saa AI tõlget teha (MAIN_LANGUAGE_NOT_TRANSLATABLE)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Koolitust, põhikeele tõlget või sihtkeelt ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "AI teenus pole saadaval (AI_SERVICE_UNAVAILABLE)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AiTrainingContentDto> translateTrainingContent(
            @Parameter(description = "Koolitus, mille salvestatud põhikeele tekstid tõlgitakse", required = true)
            @PathVariable Integer trainingId,
            @Parameter(description = "Sihtkeele ID; peab erinema põhikeelest", required = true)
            @RequestParam Integer languageId) {
        AiTrainingContentDto aiTrainingContentDto = new AiTrainingContentDto(
                "AI-ga tõlgitud pealkiri (TO BE IMPLEMENTED)",
                "AI-ga tõlgitud lühikirjeldus (TO BE IMPLEMENTED)",
                "AI-ga tõlgitud kirjeldus (TO BE IMPLEMENTED)");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(aiTrainingContentDto);
    }
}
