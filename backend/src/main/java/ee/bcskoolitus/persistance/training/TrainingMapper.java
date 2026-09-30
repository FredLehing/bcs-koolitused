package ee.bcskoolitus.persistance.training;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.controller.training.dto.TrainingUpdateRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingMapper {

    // Seosed (kategooria, keel, asukoht, kasutaja), status ja ajatemplid määrab TrainingService
    @Mapping(source = "isOrderable", target = "isOrderable")
    @Mapping(source = "isPromoted", target = "isPromoted")
    Training toTraining(TrainingCreateRequestDto trainingCreateRequestDto);

    // Seosed määrab TrainingService; user, status ja ajatemplid ei muutu
    @Mapping(source = "isOrderable", target = "isOrderable")
    @Mapping(source = "isPromoted", target = "isPromoted")
    void updateTraining(TrainingUpdateRequestDto trainingUpdateRequestDto, @MappingTarget Training training);

    @Mapping(source = "id", target = "trainingId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "trainingLanguage.id", target = "trainingLanguageId")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(target = "fundingTypeIds", ignore = true)
    @Mapping(target = "lecturers", ignore = true)
    TrainingDto toTrainingDto(Training training);
}