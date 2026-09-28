package ee.bcskoolitus.persistance.training;

import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingMapper {

    // Seosed (kategooria, keel, asukoht, lektor, kasutaja), status ja ajatemplid määrab TrainingService
    @Mapping(source = "isOrderable", target = "isOrderable")
    @Mapping(source = "isPromoted", target = "isPromoted")
    Training toTraining(TrainingCreateRequestDto trainingCreateRequestDto);

}
