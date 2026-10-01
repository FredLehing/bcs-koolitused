package ee.bcskoolitus.persistance.training;

import ee.bcskoolitus.controller.training.dto.TrainingPageDto;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingPageMapper {
    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "trainingTranslation.id", target = "trainingTranslationId")
    @Mapping(source = "training.trainingLanguage.code", target = "trainingLanguageCode")
    @Mapping(source = "training.trainingLanguage.flagIconCode", target = "trainingLanguageFlagIconCode")
    @Mapping(source = "training.location.name", target = "locationName")
    @Mapping(source = "training.location.isOnline", target = "isOnline")
    @Mapping(target = "isMainLanguageFallback", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "curriculumFileName", ignore = true)
    @Mapping(target = "curriculumFileSize", ignore = true)
    @Mapping(target = "fundingTypes", ignore = true)
    @Mapping(target = "lecturers", ignore = true)
    @Mapping(target = "upcomingCourses", ignore = true)
    TrainingPageDto toTrainingPageDto(Training training, TrainingTranslation trainingTranslation);
}
