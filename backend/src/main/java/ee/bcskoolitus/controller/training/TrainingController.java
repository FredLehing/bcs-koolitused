package ee.bcskoolitus.controller.training;

import ee.bcskoolitus.controller.training.dto.TrainingSummaryDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping("/trainings")
    @Operation(summary = "Tagastab koolituste nimekirja koos leheküljestamiseks vajaliku metainfoga")
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
                                      @RequestParam String trainingLang,
                                      @RequestParam String contentLang) {
        return trainingService.findFilteredTrainings(categoryId, fundingTypeId, limit, page, trainingLang, contentLang);
    }
}
