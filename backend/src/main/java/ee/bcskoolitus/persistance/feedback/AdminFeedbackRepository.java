package ee.bcskoolitus.persistance.feedback;

import ee.bcskoolitus.controller.adminfeedback.dto.*;
import ee.bcskoolitus.infrastructure.util.LikePatterns;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Dünaamiline JPQL on vajalik otsingu sõnade ja lubatud sortide jaoks.
// Väärtused seotakse parameetritena; JPQL-i lisatakse ainult siin määratud avaldisi.
@Repository
public class AdminFeedbackRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String TITLE = "coalesce(ct.title, mt.title)";
    private static final String COMMENT_COUNT = "sum(case when a.feedbackText is not null and length(trim(a.feedbackText)) > 0 then 1 else 0 end)";
    private static final String FROM = """
             from Feedback f
             join f.courseParticipant cp join cp.course c join c.training t join cp.participant p
             left join TrainingTranslation ct on ct.training.id = t.id and ct.language.code = :contentLang
             left join TrainingTranslation mt on mt.training.id = t.id and mt.language.code = :mainLang
             left join CourseParticipantFeedback a on a.feedback.id = f.id
            """;
    private static final String GROUP = " group by f.id, cp.id, c.id, t.id, ct.title, mt.title, p.name, c.startDate, c.endDate, f.createdAt, f.status ";

    public List<AdminFeedbackCourseDto> findAdminFeedbackCoursesBy(LocalDate today, String contentLang, String mainLang) {
        return entityManager.createQuery("""
                select new ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackCourseDto(
                    c.id, t.id, coalesce(ct.title, mt.title), c.startDate, c.endDate)
                from Course c join c.training t
                left join TrainingTranslation ct on ct.training.id = t.id and ct.language.code = :contentLang
                left join TrainingTranslation mt on mt.training.id = t.id and mt.language.code = :mainLang
                where c.endDate < :today and c.status in ('O', 'F')
                order by c.endDate desc, c.id desc
                """, AdminFeedbackCourseDto.class)
                .setParameter("today", today).setParameter("contentLang", contentLang).setParameter("mainLang", mainLang)
                .getResultList();
    }

    public AdminFeedbackPageDto findAdminFeedbackPageBy(AdminFeedbackFilterDto adminFeedbackFilterDto,
                                                       LocalDate today, String contentLang, String mainLang) {
        QueryParts queryParts = createQueryParts(adminFeedbackFilterDto, today, contentLang, mainLang);
        String eligibleFeedbackIds = "select f.id " + queryParts.groupedQuery();
        Object[] counts = bind(entityManager.createQuery("""
                select count(header), coalesce(sum(case when header.status in ('N', 'U') then 1 else 0 end), 0)
                from Feedback header where header.id in (
                """ + eligibleFeedbackIds + ")", Object[].class), queryParts).getSingleResult();
        Long lowCount = bind(entityManager.createQuery("select count(header) from Feedback header where header.id in ("
                + eligibleFeedbackIds + ") and exists (select lowAnswer.id from CourseParticipantFeedback lowAnswer where lowAnswer.feedback.id = header.id and lowAnswer.score <= 5)", Long.class), queryParts).getSingleResult();
        Double overallAverage = bind(entityManager.createQuery("select avg(answer.score) from CourseParticipantFeedback answer where answer.feedback.id in ("
                + eligibleFeedbackIds + ")", Double.class), queryParts).getSingleResult();
        List<FeedbackCriteriaSummaryDto> criteriaAverages = bind(entityManager.createQuery("""
                select new ee.bcskoolitus.controller.adminfeedback.dto.FeedbackCriteriaSummaryDto(
                    criterion.id, coalesce(contentTranslation.title, mainTranslation.title), avg(answer.score), count(answer))
                from FeedbackCriteria criterion
                left join FeedbackCriteriaTranslation contentTranslation on contentTranslation.feedbackCriteria.id = criterion.id and contentTranslation.language.code = :contentLang
                left join FeedbackCriteriaTranslation mainTranslation on mainTranslation.feedbackCriteria.id = criterion.id and mainTranslation.language.code = :mainLang
                left join CourseParticipantFeedback answer on answer.feedbackCriteria.id = criterion.id and answer.feedback.id in (
                """ + eligibleFeedbackIds + """
                )
                where criterion.status = 'A' or answer.id is not null
                group by criterion.id, criterion.sequence, contentTranslation.title, mainTranslation.title
                order by criterion.sequence, criterion.id
                """, FeedbackCriteriaSummaryDto.class), queryParts).getResultList();
        List<AdminFeedbackSummaryDto> content = bind(entityManager.createQuery("""
                select new ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackSummaryDto(
                    f.id, cp.id, c.id, t.id, coalesce(ct.title, mt.title), p.name,
                    c.startDate, c.endDate, f.createdAt, max(a.updatedAt), avg(a.score), min(a.score),
                """ + COMMENT_COUNT + ", f.status) " + queryParts.groupedQuery() + sortOrder(adminFeedbackFilterDto), AdminFeedbackSummaryDto.class), queryParts)
                .setFirstResult(Math.toIntExact(Math.min((long) adminFeedbackFilterDto.pageValue() * adminFeedbackFilterDto.limitValue(), Integer.MAX_VALUE)))
                .setMaxResults(adminFeedbackFilterDto.limitValue()).getResultList();
        long totalElements = ((Number) counts[0]).longValue();
        return new AdminFeedbackPageDto(adminFeedbackFilterDto.pageValue(),
                (int) ((totalElements + adminFeedbackFilterDto.limitValue() - 1) / adminFeedbackFilterDto.limitValue()),
                totalElements, ((Number) counts[1]).longValue(), lowCount, overallAverage,
                adminFeedbackFilterDto.courseIdValue() == null ? null : findCourseFeedbackResponseRateBy(adminFeedbackFilterDto.courseIdValue()),
                criteriaAverages, content);
    }

    public CourseFeedbackResponseRateDto findCourseFeedbackResponseRateBy(Integer courseId) {
        Object[] counts = entityManager.createQuery("""
                select count(cp), coalesce(sum(case when exists (
                    select f.id from Feedback f where f.courseParticipant.id = cp.id
                ) then 1 else 0 end), 0)
                from CourseParticipant cp where cp.course.id = :courseId and cp.status = 'R'
                """, Object[].class).setParameter("courseId", courseId).getSingleResult();
        long registeredCount = ((Number) counts[0]).longValue();
        long respondedCount = ((Number) counts[1]).longValue();
        return new CourseFeedbackResponseRateDto(courseId, registeredCount, respondedCount,
                registeredCount == 0 ? null : 100.0 * respondedCount / registeredCount);
    }

    private static QueryParts createQueryParts(AdminFeedbackFilterDto filter, LocalDate today, String contentLang, String mainLang) {
        StringBuilder where = new StringBuilder(" where c.endDate < :today ");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("today", today);
        parameters.put("contentLang", contentLang);
        parameters.put("mainLang", mainLang);
        if (filter.courseIdValue() != null) { where.append(" and c.id = :courseId "); parameters.put("courseId", filter.courseIdValue()); }
        if (!AdminFeedbackFilterDto.isEmpty(filter.getStatus())) {
            if ("pending".equals(filter.getStatus())) where.append(" and f.status in ('N', 'U') ");
            else { where.append(" and f.status = :status "); parameters.put("status", filter.getStatus()); }
        }
        if (filter.fromValue() != null) { where.append(" and c.endDate >= :fromDate "); parameters.put("fromDate", filter.fromValue()); }
        if (filter.untilValue() != null) { where.append(" and c.startDate <= :untilDate "); parameters.put("untilDate", filter.untilValue()); }
        int wordIndex = 0;
        for (String word : LikePatterns.toLowerCaseWords(filter.getSearchText())) {
            String parameter = "word" + wordIndex++;
            where.append(" and lower(concat(").append(TITLE).append(", ' ', p.name)) like :").append(parameter).append(" escape '\\' ");
            parameters.put(parameter, LikePatterns.toContainsPattern(word));
        }
        StringBuilder having = new StringBuilder(" having 1 = 1 ");
        if (filter.lowValue() != null) { having.append(" and min(a.score) <= :low "); parameters.put("low", filter.lowValue()); }
        if ("yes".equals(filter.getComments())) having.append(" and ").append(COMMENT_COUNT).append(" > 0 ");
        if ("no".equals(filter.getComments())) having.append(" and ").append(COMMENT_COUNT).append(" = 0 ");
        return new QueryParts(FROM + where + GROUP + having, parameters);
    }

    private static String sortOrder(AdminFeedbackFilterDto filter) {
        String expression = switch (filter.sortByValue()) {
            case "createdAt" -> "f.createdAt";
            case "answersUpdatedAt" -> "max(a.updatedAt)";
            case "trainingTitle" -> "lower(" + TITLE + ")";
            case "participantName" -> "lower(p.name)";
            case "averageScore" -> "avg(a.score)";
            case "minimumScore" -> "min(a.score)";
            case "status" -> "case f.status when 'N' then 0 when 'U' then 1 else 2 end";
            default -> null;
        };
        if (expression == null) return " order by case when f.status in ('N', 'U') then 0 else 1 end, f.createdAt desc, f.id desc";
        return " order by " + expression + " " + filter.sortDirectionValue() + " nulls last, f.id desc";
    }

    private static <T> TypedQuery<T> bind(TypedQuery<T> typedQuery, QueryParts queryParts) {
        queryParts.parameters().forEach(typedQuery::setParameter);
        return typedQuery;
    }
    private record QueryParts(String groupedQuery, Map<String, Object> parameters) { }
}
