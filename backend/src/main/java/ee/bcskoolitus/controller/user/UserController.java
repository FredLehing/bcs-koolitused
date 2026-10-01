package ee.bcskoolitus.controller.user;

import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.AdminUserDto;
import ee.bcskoolitus.controller.user.dto.AdminUserSummaryDto;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.PasswordChangeRequestDto;
import ee.bcskoolitus.controller.user.dto.ProfileUpdateRequestDto;
import ee.bcskoolitus.controller.user.dto.SignupRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.AdminUserService;
import ee.bcskoolitus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
public class UserController {
    private final UserService userService;
    private final AdminUserService adminUserService;

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

    @PutMapping("/user/{userId}/profile")
    @Operation(summary = "Kasutaja muudab oma andmeid (Minu andmed)",
            description = "Ühes transaktsioonis: konto e-post (sisselogimine), profiil (nimi, telefon, e-post) ja participant.name. "
                    + "Osalejata kasutajale luuakse profile + participant. E-post trimmitakse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "E-post on teisel kontol kasutusel (tõstutundetu) -> 'errorCode:' EMAIL_TAKEN",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, vigane e-post või telefon üle 20 märgi -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateProfile(@PathVariable Integer userId, @Valid @RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        userService.updateProfile(userId, profileUpdateRequestDto);
    }

    @PutMapping("/user/{userId}/password")
    @Operation(summary = "Kasutaja muudab oma parooli",
            description = "Praegune parool peab klappima, uus parool vähemalt 8 märki.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Praegune parool on vale -> 'errorCode:' INCORRECT_PASSWORD",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub või uus parool lühem kui 8 märki -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updatePassword(@PathVariable Integer userId, @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequestDto) {
        userService.updatePassword(userId, passwordChangeRequestDto);
    }

    @GetMapping("/admin-users")
    @Operation(summary = "Admini kontode nimekiri",
            description = "Uusimad eespool (created_at kahanevalt). Vaikimisi ainult aktiivsed (status A); includeDeleted=true → ka deaktiveeritud. "
                    + "participantName ja phone osalejalt (puudumisel null), registrationCount = registreerunud (R) read. Parooli ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<AdminUserSummaryDto> findAdminUsers(@RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        return adminUserService.findAdminUsers(includeDeleted);
    }

    @GetMapping("/admin-user/{userId}")
    @Operation(summary = "Ühe konto admini vaade",
            description = "Konto (ka deaktiveeritud), osaleja (puudumisel osaleja väljad null) ja registreerumised (ka loobunud, kustutatud toimumiskorrad välja, "
                    + "uusimad toimumiskorrad eespool). trainingTitle contentLang keeles, puudumisel põhikeeles. Parooli ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminUserDto getAdminUser(@PathVariable Integer userId, @RequestParam String contentLang) {
        return adminUserService.getAdminUser(userId, contentLang);
    }

    @DeleteMapping("/admin-user/{userId}")
    @Operation(summary = "Deaktiveerib konto (soft delete)",
            description = "Määrab \"user\".status = D; osaleja, profiil ja registreerumised jäävad. Deaktiveeritud konto ei saa sisse logida. "
                    + "currentUserId = sisse loginud admin, iseennast deaktiveerida ei saa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "userId = currentUserId -> 'errorCode:' CANNOT_DEACTIVATE_SELF",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deactivateUser(@PathVariable Integer userId, @RequestParam Integer currentUserId) {
        adminUserService.deactivateUser(userId, currentUserId);
    }

    @PutMapping("/admin-user/{userId}/restore")
    @Operation(summary = "Taastab deaktiveeritud konto",
            description = "Määrab \"user\".status = A; konto saab jälle sisse logida. Aktiivse konto korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu userId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void restoreUser(@PathVariable Integer userId) {
        adminUserService.restoreUser(userId);
    }
}
