package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.login.dto.LoginRequest;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.persistance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;

    public LoginResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.
        return null;
    }
}
