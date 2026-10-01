package ee.bcskoolitus.controller.aitraining;

import ee.bcskoolitus.controller.common.dto.AiTrainingContentDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai-training")
@Tag(name = "AI koolituse tekstid", description = "TO BE IMPLEMENTED: PDF-ist koostatud tekstid ja AI tõlked; tulemusi ei salvestata")
public class AiTrainingController {

    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    @Operation(summary = "TO BE IMPLEMENTED: loob PDF-ist koolituse väljade ettepaneku",
            description = "Kasutatakse olekutes new-training ja new-translation; sisend on vormis valitud salvestamata PDF. "
                    + "Saadab valitud PDF-i tulevikus Gemini AI-le ning tagastab pealkirja, lühikirjelduse ja kirjelduse. "
                    + "Vorm täidetakse vastusega, kuid andmebaasi midagi ei salvestata. Praegu tagastab meetod fikseeritud placeholder-väärtused; PDF-i ei töödelda, andmebaasi ei loeta ega Gemini AI-d kutsuta. Dokumenteeritud ärivead lisatakse tegeliku teostusega.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF-ist loodud koolituse väljade ettepanek",
                    content = @Content(schema = @Schema(implementation = AiTrainingContentDto.class))),
            @ApiResponse(responseCode = "400", description = "Fail puudub või ei ole PDF",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "413", description = "PDF ületab lubatud suuruse",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "AI teenus pole saadaval",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AiTrainingContentDto createTrainingContentFromPdf(
            @Parameter(description = "Salvestamata PDF-fail, millest koolituse väljad luuakse", required = true)
            @RequestPart("curriculum") MultipartFile curriculum) {
        return getAiTrainingContentPlaceholder();
    }

    @PostMapping(value = "/pdf/{trainingTranslationId}", consumes = "multipart/form-data")
    @Operation(summary = "TO BE IMPLEMENTED: loob õppekava PDF-ist koolituse väljade ettepaneku",
            description = "Kui päringus on PDF, kasutatakse seda (sh vormis valitud salvestamata asendusfaili). "
                    + "Kui PDF puudub, loetakse tõlke salvestatud õppekava. Gemini AI vastus täidab vormi; andmebaasi midagi ei salvestata. "
                    + "Praegu tagastab meetod fikseeritud placeholder-väärtused; PDF-i ei töödelda, andmebaasi ei loeta ega Gemini AI-d kutsuta. Dokumenteeritud ärivead lisatakse tegeliku teostusega.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF-ist loodud koolituse väljade ettepanek",
                    content = @Content(schema = @Schema(implementation = AiTrainingContentDto.class))),
            @ApiResponse(responseCode = "404", description = "Tõlget või salvestatud õppekava ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Lisatud fail ei ole PDF",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "413", description = "PDF ületab lubatud suuruse",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "AI teenus pole saadaval",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AiTrainingContentDto createTrainingContentFromTranslationCurriculum(
            @Parameter(description = "Avatud tõlke ID", required = true) @PathVariable Integer trainingTranslationId,
            @Parameter(description = "Valikuline uus PDF. Puudumisel kasutatakse andmebaasis salvestatud õppekava")
            @RequestPart(value = "curriculum", required = false) MultipartFile curriculum) {
        return getAiTrainingContentPlaceholder();
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

    private AiTrainingContentDto getAiTrainingContentPlaceholder() {
        return new AiTrainingContentDto(
                "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)",
                "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)",
                "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)");
    }
}
