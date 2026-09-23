package ee.bcskoolitus.service;

import ee.bcskoolitus.ApiStatus;
import ee.bcskoolitus.controller.login.dto.LoginRequest;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.user.UserMapper;
import ee.bcskoolitus.persistance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ee.bcskoolitus.Error.INCORRECT_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public LoginResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findUserBy(loginRequest.getEmail(), loginRequest.getPassword(), ApiStatus.STATUS_ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(INCORRECT_CREDENTIALS.getMessage(),INCORRECT_CREDENTIALS.name()));
        return userMapper.toLoginResponse(user);
    }
}
