package ee.bcskoolitus.persistance.view.trainingsummary;

import ee.bcskoolitus.infrastructure.util.LikePatterns;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Iga meetod tagastab ühe WHERE tingimuse tüki; filtri väärtus 0 või tühi tekst = tingimust ei rakendata
public class TrainingSummarySpecifications {

    public static Specification<TrainingSummary> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> categoryId == 0
                ? cb.conjunction()
                : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<TrainingSummary> hasFundingTypeId(Integer fundingTypeId) {
        return (root, query, cb) -> {
            if (fundingTypeId == 0) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<TrainingFundingType> trainingFundingType = subquery.from(TrainingFundingType.class);
            subquery.select(trainingFundingType.get("id"))
                    .where(cb.equal(trainingFundingType.get("training"), root.get("training")),
                            cb.equal(trainingFundingType.get("fundingType").get("id"), fundingTypeId));
            return cb.exists(subquery);
        };
    }

    public static Specification<TrainingSummary> hasTrainingLanguageId(Integer trainingLanguageId) {
        return (root, query, cb) -> trainingLanguageId == 0
                ? cb.conjunction()
                : cb.equal(root.get("trainingLanguageId"), trainingLanguageId);
    }

    public static Specification<TrainingSummary> hasTranslationLanguageCode(String contentLang) {
        return (root, query, cb) -> cb.equal(root.get("translationLanguageCode"), contentLang);
    }

    // Iga sõna peab esinema (contains, tõstutundetu) kas pealkirjas või lühikirjelduses
    public static Specification<TrainingSummary> containsAllWords(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> wordPredicates = new ArrayList<>();
            for (String word : LikePatterns.toLowerCaseWords(searchText)) {
                String pattern = LikePatterns.toContainsPattern(word);
                wordPredicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern, LikePatterns.ESCAPE_CHAR),
                        cb.like(cb.lower(root.get("shortDescription")), pattern, LikePatterns.ESCAPE_CHAR)));
            }
            return cb.and(wordPredicates.toArray(new Predicate[0]));
        };
    }
}
