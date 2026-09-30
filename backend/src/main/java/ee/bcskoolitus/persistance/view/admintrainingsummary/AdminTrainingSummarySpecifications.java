package ee.bcskoolitus.persistance.view.admintrainingsummary;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.util.LikePatterns;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Iga meetod tagastab ühe WHERE tingimuse tüki; ID filtri väärtus 0 või valikulise filtri null = tingimust ei rakendata
public class AdminTrainingSummarySpecifications {

    public static Specification<AdminTrainingSummary> hasContentLanguageCode(String contentLang) {
        return (root, query, cb) -> cb.equal(root.get("contentLanguageCode"), contentLang);
    }

    // null = aktiivsed koolitused (mustand ja publitseeritud), kustutatud ainult status = D korral
    public static Specification<AdminTrainingSummary> hasStatus(String status) {
        return (root, query, cb) -> status == null
                ? root.get("status").in(TrainingStatus.UNPUBLISHED.getCode(), TrainingStatus.PUBLISHED.getCode())
                : cb.equal(root.get("status"), status);
    }

    public static Specification<AdminTrainingSummary> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> categoryId == 0
                ? cb.conjunction()
                : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<AdminTrainingSummary> hasTrainingLanguageId(Integer trainingLanguageId) {
        return (root, query, cb) -> trainingLanguageId == 0
                ? cb.conjunction()
                : cb.equal(root.get("trainingLanguageId"), trainingLanguageId);
    }

    public static Specification<AdminTrainingSummary> hasFundingTypeId(Integer fundingTypeId) {
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

    public static Specification<AdminTrainingSummary> hasIsOrderable(Boolean isOrderable) {
        return (root, query, cb) -> isOrderable == null
                ? cb.conjunction()
                : cb.equal(root.get("isOrderable"), isOrderable);
    }

    public static Specification<AdminTrainingSummary> hasIsPromoted(Boolean isPromoted) {
        return (root, query, cb) -> isPromoted == null
                ? cb.conjunction()
                : cb.equal(root.get("isPromoted"), isPromoted);
    }

    public static Specification<AdminTrainingSummary> hasAllTranslations(Boolean hasAllTranslations) {
        return (root, query, cb) -> hasAllTranslations == null
                ? cb.conjunction()
                : cb.equal(root.get("hasAllTranslations"), hasAllTranslations);
    }

    // Iga sõna peab esinema (contains, tõstutundetu) pealkirjas — erinevalt avalikust otsingust mitte lühikirjelduses
    public static Specification<AdminTrainingSummary> titleContainsAllWords(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> wordPredicates = new ArrayList<>();
            for (String word : LikePatterns.toLowerCaseWords(searchText)) {
                wordPredicates.add(cb.like(cb.lower(root.get("title")), LikePatterns.toContainsPattern(word), LikePatterns.ESCAPE_CHAR));
            }
            return cb.and(wordPredicates.toArray(new Predicate[0]));
        };
    }
}
