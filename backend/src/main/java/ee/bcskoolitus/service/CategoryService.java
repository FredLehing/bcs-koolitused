package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.CategoryDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.category.CategoryRepository;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationMapper;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final CategoryTranslationMapper categoryTranslationMapper;

    public Category getValidCategoryBy(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("categoryId", categoryId));
    }

    public List<CategoryDto> getCategories(String contentLang) {
        return categoryTranslationMapper.toCategoryDtos(
                categoryTranslationRepository.findAllByLanguage_CodeOrderByCategory_IdAsc(contentLang)
        );
    }
}