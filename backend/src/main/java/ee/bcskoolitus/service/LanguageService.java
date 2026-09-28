package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.language.LanguageMapper;
import ee.bcskoolitus.persistance.language.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    public List<SystemLanguageDto> findLanguages() {
        List<Language> allLanguages = languageRepository.findAllLanguages();
        return languageMapper.toSystemLanguageDtos(allLanguages);
    }

    public Language getValidLanguageBy(Integer languageId, String fieldName) {
        return languageRepository.findById(languageId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException(fieldName, languageId));
    }

    // Põhikeel (language.is_main_language = true) on andmete invariant — selle puudumine on serveri viga (500)
    public Language getMainLanguage() {
        return languageRepository.findMainLanguage()
                .orElseThrow(() -> new IllegalStateException("Põhikeel puudub (language.is_main_language = true)"));
    }
}
