package ee.bcskoolitus.persistance.language;

import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LanguageMapper {

    @Mapping(source = "id", target = "languageId")
    @Mapping(source = "code", target = "languageCode")
    @Mapping(source = "name", target = "languageName")
    SystemLanguageDto toSystemLanguageDto(Language language);

    List<SystemLanguageDto> toSystemLanguageDtos(List<Language> languages);

}