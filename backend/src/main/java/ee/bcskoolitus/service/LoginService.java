package ee.bcskoolitus.service;

import ee.bcskoolitus.ApiStatus;
import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
import ee.bcskoolitus.controller.login.dto.LoginRequest;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.language.LanguageMapper;
import ee.bcskoolitus.persistance.language.LanguageRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.user.UserMapper;
import ee.bcskoolitus.persistance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.bcskoolitus.ApiRole.ROLE_ADMIN;
import static ee.bcskoolitus.Error.INCORRECT_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;


    public LoginResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findUserBy(loginRequest.getEmail(), loginRequest.getPassword(), ApiStatus.STATUS_ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(INCORRECT_CREDENTIALS.getMessage(), INCORRECT_CREDENTIALS.name()));
        LoginResponse loginResponse = userMapper.toLoginResponse(user);
        handleSetSystemLanguages(user, loginResponse);
        return loginResponse;
    }

    private void handleSetSystemLanguages(User user, LoginResponse loginResponse) {
        if (ROLE_ADMIN.getName().equals(user.getRole().getName())) {
            List<SystemLanguageDto> systemLanguageDtos = getSystemLanguageDtos();
            loginResponse.setSystemLanguages(systemLanguageDtos);
        }
    }

    private List<SystemLanguageDto> getSystemLanguageDtos() {
        List<Language> languages = languageRepository.findAll();
        return languageMapper.toSystemLanguageDtos(languages);
    }
}
