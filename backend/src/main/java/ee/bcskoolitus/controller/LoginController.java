package ee.bcskoolitus.controller;

import ee.bcskoolitus.controller.login.dto.LoginRequest;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Sisse logimine tagastab userId ja roleName",
            description = "Süsteemist otsitakse emaili ja passwordi abil kasutajat, kelle konto on aktiivne. Kui vastet ei leita vistakse viga errorCode'ga INCORRECT_CREDENTIALS")
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ebaõnnestunud sisselogimisel kuvatakse -> 'message:' Vale email või parool; 'errorCode:' INCORRECT_CREDENTIALS",
                    content = @Content( schema = @Schema(implementation = ApiError.class))
            )
    })

    public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {
        return loginService.loginUser(loginRequest);
    }
}
