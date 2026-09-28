package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category getValidCategoryBy(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("categoryId", categoryId));
    }
}
