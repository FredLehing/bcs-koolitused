package ee.bcskoolitus.controller.training;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateResponseDto;
import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingService;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping("/trainings")
    @Operation(summary = "Tagastab koolituste nimekirja koos leheküljestamiseks vajaliku metainfoga",
            description = "searchText: tühikutega eraldatud sõnad, millest iga peab esinema (contains, tõstutundetu) koolituse pealkirjas või lühikirjelduses. Tühi string = otsingut ei rakendata.")
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Integer väljale lisatakse String, mis põhjustab veateate",
                    content = @Content( schema = @Schema(implementation = ApiError.class))
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
    public TrainingDto getTraining(@PathVariable Integer trainingId) {
        return trainingService.getTraining(trainingId);

    }

    @GetMapping("/training/{trainingId}/training-translations")
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
}
