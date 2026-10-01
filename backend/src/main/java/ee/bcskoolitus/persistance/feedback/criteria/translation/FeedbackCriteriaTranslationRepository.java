package ee.bcskoolitus.persistance.feedback.criteria.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackCriteriaTranslationRepository extends JpaRepository<FeedbackCriteriaTranslation, Integer> {

    // Kriteeriumide tõlked antud keeltes (contentLang ja põhikeel)
    @Query("""
            select fct from FeedbackCriteriaTranslation fct
            join fetch fct.language l
            where fct.feedbackCriteria.id in :feedbackCriteriaIds
              and l.code in :languageCodes""")
    List<FeedbackCriteriaTranslation> findFeedbackCriteriaTranslationsBy(List<Integer> feedbackCriteriaIds, List<String> languageCodes);
}
