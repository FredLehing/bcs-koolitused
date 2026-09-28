package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
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
}
