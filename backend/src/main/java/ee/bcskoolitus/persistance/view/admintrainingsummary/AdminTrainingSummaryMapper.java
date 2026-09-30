package ee.bcskoolitus.persistance.view.admintrainingsummary;

import ee.bcskoolitus.controller.training.dto.TrainingTitleDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminTrainingSummaryMapper {

    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "title", target = "title")
    TrainingTitleDto toTrainingTitleDto(AdminTrainingSummary adminTrainingSummary);

    List<TrainingTitleDto> toTrainingTitleDtos(List<AdminTrainingSummary> adminTrainingSummaries);
}
