package ee.bcskoolitus.controller.fundingtype;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.service.FundingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FundingTypeController {

    private final FundingTypeService fundingTypeService;

    @GetMapping("/funding-types")
    @Operation(summary = "Tagastab rahastustüübid valitud sisukeele järgi")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            )
    })
    public List<FundingTypeDto> getFundingTypes(@RequestParam String contentLang) {
        return fundingTypeService.getFundingTypes(contentLang);
    }
}