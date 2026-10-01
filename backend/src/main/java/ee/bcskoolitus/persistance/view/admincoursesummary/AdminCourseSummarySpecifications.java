package ee.bcskoolitus.persistance.view.admincoursesummary;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.infrastructure.util.LikePatterns;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Iga meetod tagastab ühe WHERE tingimuse tüki; valikulise filtri null (ID filtril ka 0) = tingimust ei rakendata
public class AdminCourseSummarySpecifications {

    public static final String ATTENDANCE_ONSITE = "ONSITE";
    public static final String ATTENDANCE_ONLINE = "ONLINE";

    public static Specification<AdminCourseSummary> hasContentLanguageCode(String contentLang) {
        return (root, query, cb) -> cb.equal(root.get("contentLanguageCode"), contentLang);
    }

    // Kustutatud koolituse toimumiskordi ei kuvata kunagi
    public static Specification<AdminCourseSummary> hasActiveTraining() {
        return (root, query, cb) -> cb.notEqual(root.get("trainingStatus"), TrainingStatus.DELETED.getCode());
    }

    // null = kõik peale kustutatud (D)
    public static Specification<AdminCourseSummary> hasStatus(String status) {
        return (root, query, cb) -> status == null
                ? cb.notEqual(root.get("status"), CourseStatus.DELETED.getCode())
                : cb.equal(root.get("status"), status);
    }

    public static Specification<AdminCourseSummary> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> categoryId == null || categoryId == 0
                ? cb.conjunction()
                : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<AdminCourseSummary> hasTrainingLanguageId(Integer trainingLanguageId) {
        return (root, query, cb) -> trainingLanguageId == null || trainingLanguageId == 0
                ? cb.conjunction()
                : cb.equal(root.get("trainingLanguageId"), trainingLanguageId);
    }

    // ONSITE → ruum määratud, ONLINE → veebilink määratud; hübriid sobib mõlemaga
    public static Specification<AdminCourseSummary> hasAttendance(String attendance) {
        return (root, query, cb) -> {
            if (ATTENDANCE_ONSITE.equals(attendance)) {
                return cb.isTrue(root.get("isOnSite"));
            }
            if (ATTENDANCE_ONLINE.equals(attendance)) {
                return cb.isTrue(root.get("hasMeetingLink"));
            }
            return cb.conjunction();
        };
    }

    public static Specification<AdminCourseSummary> hasIsPromoted(Boolean isPromoted) {
        return (root, query, cb) -> isPromoted == null
                ? cb.conjunction()
                : cb.equal(root.get("isPromoted"), isPromoted);
    }

    public static Specification<AdminCourseSummary> startsOnOrAfter(LocalDate startDateFrom) {
        return (root, query, cb) -> startDateFrom == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom);
    }

    public static Specification<AdminCourseSummary> startsOnOrBefore(LocalDate startDateTo) {
        return (root, query, cb) -> startDateTo == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("startDate"), startDateTo);
    }

    // includePast = false → ainult toimumiskorrad, mille lõpp pole möödas
    public static Specification<AdminCourseSummary> isPastIncluded(Boolean includePast) {
        return (root, query, cb) -> Boolean.TRUE.equals(includePast)
                ? cb.conjunction()
                : cb.isFalse(root.get("isPast"));
    }

    // Iga sõna peab esinema (contains, tõstutundetu) koolituse nimes
    public static Specification<AdminCourseSummary> trainingTitleContainsAllWords(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> wordPredicates = new ArrayList<>();
            for (String word : LikePatterns.toLowerCaseWords(searchText)) {
                wordPredicates.add(cb.like(cb.lower(root.get("trainingTitle")), LikePatterns.toContainsPattern(word), LikePatterns.ESCAPE_CHAR));
            }
            return cb.and(wordPredicates.toArray(new Predicate[0]));
        };
    }
}
