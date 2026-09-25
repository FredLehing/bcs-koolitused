package ee.bcskoolitus.persistance.view.trainingsummary;

import ee.bcskoolitus.controller.training.dto.TrainingSummaryItemDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingSummaryMapper {

    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "trainingLanguageCode", target = "trainingLanguageCode")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "categoryId", target = "categoryId")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "training.isOrderOnly", target = "isOrderOnly")
    @Mapping(source = "training.isPromoted", target = "isPromoted")
    @Mapping(target = "fundingTypes", ignore = true)
    TrainingSummaryItemDto toTrainingSummaryItemDto(TrainingSummary trainingSummary);

    List<TrainingSummaryItemDto> toTrainingSummaryItemDtos(List<TrainingSummary> trainingSummaries);

}
