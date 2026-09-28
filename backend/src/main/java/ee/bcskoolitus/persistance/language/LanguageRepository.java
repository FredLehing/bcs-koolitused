package ee.bcskoolitus.persistance.language;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
    @Query("select l from Language l order by l.isMainLanguage desc, l.id")
    List<Language> findAllLanguages();

    @Query("select l from Language l where l.isMainLanguage = true")
    Optional<Language> findMainLanguage();
}
