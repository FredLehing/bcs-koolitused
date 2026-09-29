package ee.bcskoolitus.persistance.view.trainingsummary;

import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Iga meetod tagastab ühe WHERE tingimuse tüki; filtri väärtus 0 või tühi tekst = tingimust ei rakendata
public class TrainingSummarySpecifications {

    private static final char LIKE_ESCAPE_CHAR = '\\';

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
            if (searchText == null || searchText.isBlank()) {
                return cb.conjunction();
            }
            List<Predicate> wordPredicates = new ArrayList<>();
            for (String word : searchText.trim().toLowerCase().split("\\s+")) {
                String pattern = "%" + escapeLikeWildcards(word) + "%";
                wordPredicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern, LIKE_ESCAPE_CHAR),
                        cb.like(cb.lower(root.get("shortDescription")), pattern, LIKE_ESCAPE_CHAR)));
            }
            return cb.and(wordPredicates.toArray(new Predicate[0]));
        };
    }

    // Kasutaja sisestatud % ja _ otsitakse sõna-sõnalt, mitte LIKE metamärkidena
    private static String escapeLikeWildcards(String word) {
        return word.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
