package ee.bcskoolitus.controller;

import ee.bcskoolitus.controller.login.dto.LoginRequest;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.service.LoginService;
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
    public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {
        return loginService.loginUser(loginRequest);
    }
}
