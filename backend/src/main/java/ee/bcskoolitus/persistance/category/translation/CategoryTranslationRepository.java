package ee.bcskoolitus.persistance.category.translation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Integer> {

    List<CategoryTranslation> findAllByLanguage_CodeOrderByCategory_IdAsc(String contentLang);

    Optional<CategoryTranslation> findByCategory_IdAndLanguage_Id(Integer categoryId, Integer languageId);
}