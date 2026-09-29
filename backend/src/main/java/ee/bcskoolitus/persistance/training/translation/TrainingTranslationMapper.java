package ee.bcskoolitus.persistance.training.translation;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingTranslationItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import ee.bcskoolitus.controller.trainingtranslation.dto.TrainingTranslationDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingTranslationMapper {

    // Koolitus, keel ja ajatemplid määrab TrainingService
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    TrainingTranslation toTrainingTranslation(TrainingCreateRequestDto trainingCreateRequestDto);

    // Koolitus, keel ja ajatemplid ei muutu; description puhastab TrainingService
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    void updateTrainingTranslation(TrainingUpdateRequestDto trainingUpdateRequestDto,
                                   @MappingTarget TrainingTranslation trainingTranslation);

    @Mapping(source = "id", target = "trainingTranslationId")
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.code", target = "languageCode")
    @Mapping(source = "language.isMainLanguage", target = "isMainLanguage")
    TrainingTranslationItemDto toTrainingTranslationItemDto(TrainingTranslation trainingTranslation);

    List<TrainingTranslationItemDto> toTrainingTranslationItemDtos(
            List<TrainingTranslation> trainingTranslations
    );

    @Mapping(source = "id", target = "trainingTranslationId")
    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.code", target = "languageCode")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    TrainingTranslationDto toTrainingTranslationDto(TrainingTranslation trainingTranslation);
}