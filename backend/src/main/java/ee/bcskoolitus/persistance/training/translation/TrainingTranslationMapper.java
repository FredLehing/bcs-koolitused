package ee.bcskoolitus.persistance.training.translation;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingTranslationMapper {

    // Koolitus, keel ja ajatemplid määrab TrainingService
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "description", target = "description")
    TrainingTranslation toTrainingTranslation(TrainingCreateRequestDto trainingCreateRequestDto);

}
