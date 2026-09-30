package ee.bcskoolitus.persistance.view.admintrainingsummary;

import ee.bcskoolitus.controller.training.dto.AdminTrainingDto;
import ee.bcskoolitus.controller.training.dto.AdminTrainingSummaryItemDto;
import ee.bcskoolitus.controller.training.dto.TrainingTitleDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminTrainingSummaryMapper {

    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "title", target = "title")
    TrainingTitleDto toTrainingTitleDto(AdminTrainingSummary adminTrainingSummary);

    List<TrainingTitleDto> toTrainingTitleDtos(List<AdminTrainingSummary> adminTrainingSummaries);

    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "trainingTranslationId", target = "trainingTranslationId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "categoryId", target = "categoryId")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "trainingLanguageCode", target = "trainingLanguageCode")
    @Mapping(source = "trainingLanguageFlagIconCode", target = "trainingLanguageFlagIconCode")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isOrderable", target = "isOrderable")
    @Mapping(source = "isPromoted", target = "isPromoted")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "hasAllTranslations", target = "hasAllTranslations")
    @Mapping(source = "missingTranslationLanguageCodes", target = "missingTranslationLanguageCodes")
    @Mapping(target = "fundingTypes", ignore = true)
    AdminTrainingSummaryItemDto toAdminTrainingSummaryItemDto(AdminTrainingSummary adminTrainingSummary);

    List<AdminTrainingSummaryItemDto> toAdminTrainingSummaryItemDtos(List<AdminTrainingSummary> adminTrainingSummaries);

    @Mapping(source = "training.id", target = "trainingId")
    @Mapping(source = "trainingTranslationId", target = "trainingTranslationId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "trainingLanguageCode", target = "trainingLanguageCode")
    @Mapping(source = "trainingLanguageFlagIconCode", target = "trainingLanguageFlagIconCode")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isOrderable", target = "isOrderable")
    @Mapping(source = "isPromoted", target = "isPromoted")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "locationName", ignore = true)
    @Mapping(target = "lecturers", ignore = true)
    @Mapping(target = "fundingTypes", ignore = true)
    AdminTrainingDto toAdminTrainingDto(AdminTrainingSummary adminTrainingSummary);

    // View veerg on komaga eraldatud keelekoodid (nt "en"); null = kõik tõlked olemas → tühi list
    default List<String> toLanguageCodes(String languageCodes) {
        return languageCodes == null ? List.of() : List.of(languageCodes.split(","));
    }
}
