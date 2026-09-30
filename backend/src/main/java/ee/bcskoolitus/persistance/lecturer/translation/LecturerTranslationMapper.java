package ee.bcskoolitus.persistance.lecturer.translation;

import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationItemDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationUpdateDto;
import ee.bcskoolitus.controller.lecturertranslation.dto.LecturerTranslationDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LecturerTranslationMapper {

    // Koolitaja, keel ja ajatemplid määrab LecturerService; description puhastab LecturerService
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    LecturerTranslation toLecturerTranslation(LecturerCreateRequestDto lecturerCreateRequestDto);

    // Koolitaja, keel ja ajatemplid määrab LecturerService; description puhastab LecturerService
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    LecturerTranslation toLecturerTranslation(LecturerTranslationCreateRequestDto lecturerTranslationCreateRequestDto);

    // Koolitaja, keel ja ajatemplid ei muutu; description puhastab LecturerService
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    void updateLecturerTranslation(LecturerTranslationUpdateDto lecturerTranslationUpdateDto,
                                   @MappingTarget LecturerTranslation lecturerTranslation);

    @Mapping(source = "id", target = "lecturerTranslationId")
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.code", target = "languageCode")
    @Mapping(source = "language.isMainLanguage", target = "isMainLanguage")
    LecturerTranslationItemDto toLecturerTranslationItemDto(LecturerTranslation lecturerTranslation);

    List<LecturerTranslationItemDto> toLecturerTranslationItemDtos(List<LecturerTranslation> lecturerTranslations);

    @Mapping(source = "id", target = "lecturerTranslationId")
    @Mapping(source = "lecturer.id", target = "lecturerId")
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.code", target = "languageCode")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    LecturerTranslationDto toLecturerTranslationDto(LecturerTranslation lecturerTranslation);
}
