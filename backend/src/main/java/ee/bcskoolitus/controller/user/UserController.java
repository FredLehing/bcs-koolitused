package ee.bcskoolitus.controller.user;

import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.SignupRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/user")
    @Operation(summary = "Konto loomine (osaleja)",
            description = "Loob ühes transaktsioonis kasutaja (roll participant, status A), profiili ja osaleja. Vastus nagu sisselogimisel (userId, roleName).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Selle e-postiga konto on juba olemas (tõstutundetu) -> 'errorCode:' EMAIL_TAKEN",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, vigane e-post või parool lühem kui 8 märki -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public LoginResponse addUser(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return userService.addUser(signupRequestDto);
    }

    @GetMapping("/user/{userId}/participant")
    @Operation(summary = "Kasutaja osaleja andmed registreerumise vormi eeltäitmiseks",
            description = "Osalejata kasutajal participantId = null, nimed ja telefon \"\", email = kasutaja e-post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public MyParticipantDto getMyParticipant(@PathVariable Integer userId) {
        return userService.getMyParticipant(userId);
    }
}
