package ee.bcskoolitus.controller.language;

import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
import ee.bcskoolitus.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;

    @GetMapping("/languages")
    @Operation(summary = "Tagastab kõik keeled, primaarne on eesti keel")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            )
    })
    public List<SystemLanguageDto> findLanguages() {
        return languageService.findLanguages();
    }
}
