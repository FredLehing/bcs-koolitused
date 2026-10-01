package ee.bcskoolitus.persistance.view.publiccoursesummary;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.infrastructure.util.LikePatterns;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Iga meetod tagastab ühe WHERE tingimuse tüki; ID filtri 0 või null ja valikulise filtri null = tingimust ei rakendata
public class PublicCourseSummarySpecifications {

    public static final String ATTENDANCE_ONSITE = "ONSITE";
    public static final String ATTENDANCE_ONLINE = "ONLINE";

    public static Specification<PublicCourseSummary> hasContentLanguageCode(String contentLang) {
        return (root, query, cb) -> cb.equal(root.get("contentLanguageCode"), contentLang);
    }

    public static Specification<PublicCourseSummary> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> categoryId == null || categoryId == 0
                ? cb.conjunction()
                : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<PublicCourseSummary> hasTrainingLanguageId(Integer trainingLanguageId) {
        return (root, query, cb) -> trainingLanguageId == null || trainingLanguageId == 0
                ? cb.conjunction()
                : cb.equal(root.get("trainingLanguageId"), trainingLanguageId);
    }

    public static Specification<PublicCourseSummary> hasFundingTypeId(Integer fundingTypeId) {
        return (root, query, cb) -> {
            if (fundingTypeId == null || fundingTypeId == 0) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<TrainingFundingType> trainingFundingType = subquery.from(TrainingFundingType.class);
            subquery.select(trainingFundingType.get("id"))
                    .where(cb.equal(trainingFundingType.get("training").get("id"), root.get("trainingId")),
                            cb.equal(trainingFundingType.get("fundingType").get("id"), fundingTypeId));
            return cb.exists(subquery);
        };
    }

    // ONSITE → ruum määratud, ONLINE → veebilink määratud; hübriid sobib mõlemaga
    public static Specification<PublicCourseSummary> hasAttendance(String attendance) {
        return (root, query, cb) -> {
            if (ATTENDANCE_ONSITE.equals(attendance)) {
                return cb.isTrue(root.get("isOnSite"));
            }
            if (ATTENDANCE_ONLINE.equals(attendance)) {
                return cb.isTrue(root.get("isOnline"));
            }
            return cb.conjunction();
        };
    }

    // hideFull = true → täis (F) toimumiskorrad välja
    public static Specification<PublicCourseSummary> isFullIncluded(Boolean hideFull) {
        return (root, query, cb) -> Boolean.TRUE.equals(hideFull)
                ? cb.notEqual(root.get("status"), CourseStatus.FULL.getCode())
                : cb.conjunction();
    }

    public static Specification<PublicCourseSummary> startsOnOrAfter(LocalDate startDateFrom) {
        return (root, query, cb) -> startDateFrom == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom);
    }

    public static Specification<PublicCourseSummary> startsOnOrBefore(LocalDate startDateTo) {
        return (root, query, cb) -> startDateTo == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("startDate"), startDateTo);
    }

    // Iga sõna peab esinema (contains, tõstutundetu) kas pealkirjas või lühikirjelduses
    public static Specification<PublicCourseSummary> containsAllWords(String searchText) {
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
