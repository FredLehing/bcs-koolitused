package ee.bcskoolitus.persistance.language;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
    @Query("select l from Language l order by l.isMainLanguage desc, l.id")
    List<Language> findAllLanguages();
}