package ee.bcskoolitus.persistance.category.translation;

import ee.bcskoolitus.controller.common.dto.CategoryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryTranslationMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "name", target = "categoryName")
    CategoryDto toCategoryDto(CategoryTranslation categoryTranslation);

    List<CategoryDto> toCategoryDtos(List<CategoryTranslation> categoryTranslations);
}