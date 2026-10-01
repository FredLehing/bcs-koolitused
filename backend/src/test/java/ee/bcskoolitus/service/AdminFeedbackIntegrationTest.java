package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackDto;
import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackFilterDto;
import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackPageDto;
import ee.bcskoolitus.controller.feedback.dto.FeedbackAnswerDto;
import ee.bcskoolitus.controller.feedback.dto.FeedbackRequestDto;
import ee.bcskoolitus.infrastructure.exception.ConflictException;
import ee.bcskoolitus.infrastructure.exception.IncorrectInputException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedbackRepository;
import ee.bcskoolitus.persistance.feedback.*;
import ee.bcskoolitus.persistance.feedback.criteria.FeedbackCriteriaRepository;
import ee.bcskoolitus.persistance.feedback.criteria.translation.FeedbackCriteriaTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Eraldatud H2 PostgreSQL-režiimis; arendaja PostgreSQL-i ei ühendata ega muudeta.
@SpringJUnitConfig(AdminFeedbackIntegrationTest.Config.class)
class AdminFeedbackIntegrationTest {
    @Autowired AdminFeedbackService adminFeedbackService;
    @Autowired FeedbackService feedbackService;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired DataSource dataSource;

    @BeforeEach
    void importApprovedSeed() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        Set<String> tables = Set.of("category", "category_translation", "language", "role", "user", "profile", "participant", "location", "training", "training_translation", "room", "course", "course_participant", "feedback", "feedback_criteria", "feedback_criteria_translation", "course_participant_feedback");
        for (String table : tables) jdbcTemplate.execute("DELETE FROM bcs_koolitused.\"" + table + "\"");
        String seed = Files.readString(Path.of("../docs/database/3_import.sql"));
        seed = seed.replaceAll("(?m)--[^\\n]*", "");
        StringBuilder statement = new StringBuilder();
        boolean quoted = false;
        Pattern tablePattern = Pattern.compile("^\\s*INSERT INTO\\s+(\\\"?\\w+\\\"?)", Pattern.CASE_INSENSITIVE);
        for (char character : seed.toCharArray()) {
            statement.append(character);
            if (character == '\'') quoted = !quoted;
            if (character != ';' || quoted) continue;
            Matcher matcher = tablePattern.matcher(statement);
            if (matcher.find()) {
                String table = matcher.group(1).replace("\"", "");
                if (tables.contains(table)) jdbcTemplate.execute(statement.toString().replaceFirst("INSERT INTO\\s+\\\"?\\w+\\\"?", "INSERT INTO bcs_koolitused.\"" + table + "\""));
            }
            statement.setLength(0);
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void pageAndAggregatesMatchDmlAndStayIndependentOfPage() {
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto();
        AdminFeedbackPageDto first = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(30L, first.getTotalElements());
        assertEquals(14L, first.getNeedsReviewCount());
        assertEquals(4L, first.getLowScoreFeedbackCount());
        assertEquals(8.653333333333334, first.getOverallAverageScore(), 1e-12);
        assertEquals(List.of(18, 12, 23, 20, 11), first.getContent().stream().map(row -> row.getFeedbackId()).toList());
        assertEquals(30L, first.getCriteriaAverages().getFirst().getAnswerCount());
        filter.setPage("1");
        AdminFeedbackPageDto second = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(List.of(16, 13, 29, 7, 3), second.getContent().stream().map(row -> row.getFeedbackId()).toList());
        assertEquals(first.getOverallAverageScore(), second.getOverallAverageScore());
        filter.setPage("100");
        assertTrue(adminFeedbackService.getAdminFeedbackPage(filter).getContent().isEmpty());
    }

    @Test
    void queryCountDoesNotGrowWithPageSizeAndEmptyAnswersAreNotZero() {
        org.hibernate.stat.Statistics statistics = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto();
        filter.setLimit("1"); statistics.clear();
        adminFeedbackService.getAdminFeedbackPage(filter);
        long oneRowQueries = statistics.getPrepareStatementCount();
        filter.setLimit("100"); statistics.clear();
        adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(oneRowQueries, statistics.getPrepareStatementCount());
        assertEquals(5, oneRowQueries);
        new JdbcTemplate(dataSource).update("delete from bcs_koolitused.course_participant_feedback where feedback_id=3");
        filter.setSearchText("SQL Anna");
        AdminFeedbackPageDto page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertNull(page.getOverallAverageScore());
        assertNull(page.getContent().getFirst().getAverageScore());
        assertNull(page.getContent().getFirst().getMinimumScore());
        assertNull(page.getContent().getFirst().getAnswersUpdatedAt());
        AdminFeedbackDto detail = adminFeedbackService.getAdminFeedback(3, "et");
        assertTrue(detail.getCriteria().isEmpty()); assertNotNull(detail.getAnswersVersion());
    }

    @Test
    void filtersSortingLanguageFallbackAndEmptyResults() {
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto();
        filter.setSearchText("sql ANNA"); filter.setStatus("pending"); filter.setComments("yes");
        filter.setLow("5"); filter.setFrom("2026-09-23"); filter.setUntil("2026-09-23"); filter.setContentLang("missing");
        AdminFeedbackPageDto page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(List.of(3), page.getContent().stream().map(row -> row.getFeedbackId()).toList());
        assertEquals("SQL ja andmebaasid", page.getContent().getFirst().getTrainingTitle());
        filter.setSearchText("SQL%");
        page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(0L, page.getTotalElements()); assertEquals(0, page.getTotalPages()); assertNull(page.getOverallAverageScore());
        assertEquals(5, page.getCriteriaAverages().size()); assertEquals(0L, page.getCriteriaAverages().getFirst().getAnswerCount());
        for (String sort : List.of("createdAt", "answersUpdatedAt", "trainingTitle", "participantName", "averageScore", "minimumScore", "status")) {
            filter = new AdminFeedbackFilterDto(); filter.setSortBy(sort); filter.setSortDirection("asc");
            assertEquals(5, adminFeedbackService.getAdminFeedbackPage(filter).getContent().size());
            filter.setSortDirection("desc"); assertEquals(5, adminFeedbackService.getAdminFeedbackPage(filter).getContent().size());
        }
        filter = new AdminFeedbackFilterDto(); filter.setFrom("2026-10-01"); filter.setUntil("2026-09-01");
        AdminFeedbackFilterDto invalid = filter;
        assertThrows(IncorrectInputException.class, () -> adminFeedbackService.getAdminFeedbackPage(invalid));
    }

    @Test
    void courseChoiceIncludesUnansweredAndRateIgnoresOtherFilters() {
        assertEquals(List.of(15, 16, 14, 12, 3, 7), adminFeedbackService.getAdminFeedbackCourses("et").stream().map(course -> course.getCourseId()).toList());
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto(); filter.setCourseId("12"); filter.setStatus("N");
        AdminFeedbackPageDto page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(1L, page.getTotalElements()); assertEquals(5L, page.getCourseResponseRate().getRegisteredCount());
        assertEquals(4L, page.getCourseResponseRate().getRespondedCount()); assertEquals(80.0, page.getCourseResponseRate().getResponsePercentage());
        filter.setCourseId("15"); page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(0L, page.getTotalElements()); assertEquals(0.0, page.getCourseResponseRate().getResponsePercentage());
    }

    @Test
    void valiItExamplesIncludeSixteenResponsesAndSparseLongComments() {
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto();
        filter.setCourseId("16"); filter.setLimit("100");
        AdminFeedbackPageDto page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(16L, page.getTotalElements());
        assertEquals(18L, page.getCourseResponseRate().getRegisteredCount());
        assertEquals(16L, page.getCourseResponseRate().getRespondedCount());
        assertEquals(7, page.getContent().stream().mapToInt(row -> row.getCommentCount().intValue()).sum());
        assertTrue(page.getContent().stream().allMatch(row -> row.getTrainingTitle().equals("Vali-IT Noorem AI arendaja")));
        for (int feedbackId : List.of(10, 12, 15, 18)) {
            AdminFeedbackDto detail = adminFeedbackService.getAdminFeedback(feedbackId, "et");
            List<String> comments = detail.getCriteria().stream().map(answer -> answer.getFeedbackText()).filter(text -> text != null && !text.isBlank()).toList();
            assertEquals(1, comments.size());
            assertTrue(comments.getFirst().length() > 700);
            assertTrue(comments.getFirst().replace("\r", "").contains("\n\n"));
        }
    }

    @Test
    void detailVersionReviewAndIdempotencyUseActualAnswerTimes() {
        AdminFeedbackDto detail = adminFeedbackService.getAdminFeedback(3, "et");
        assertEquals("aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678", detail.getAnswersVersion());
        assertEquals(Instant.parse("2026-09-28T11:00:00Z"), detail.getAnswersUpdatedAt());
        assertEquals(detail.getAnswersVersion(), adminFeedbackService.getAdminFeedback(3, "en").getAnswersVersion());
        adminFeedbackService.reviewAdminFeedback(3, detail.getAnswersVersion());
        AdminFeedbackDto reviewed = adminFeedbackService.getAdminFeedback(3, "et");
        assertEquals("H", reviewed.getStatus()); assertEquals(detail.getAnswersUpdatedAt(), reviewed.getAnswersUpdatedAt());
        Instant updatedAt = feedbackRepository.findById(3).orElseThrow().getUpdatedAt();
        adminFeedbackService.reviewAdminFeedback(3, reviewed.getAnswersVersion());
        assertEquals(updatedAt, feedbackRepository.findById(3).orElseThrow().getUpdatedAt());
        assertThrows(ConflictException.class, () -> adminFeedbackService.reviewAdminFeedback(3, "0".repeat(64)));
        assertThrows(IncorrectInputException.class, () -> adminFeedbackService.reviewAdminFeedback(3, null));
    }

    @Test
    void historicalDataAndDeletedCriteriaRemainVisible() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("update bcs_koolitused.feedback_criteria set status='D' where id=1");
        jdbcTemplate.update("update bcs_koolitused.training set status='D' where id=8");
        jdbcTemplate.update("update bcs_koolitused.course set status='D' where id=14");
        jdbcTemplate.update("update bcs_koolitused.course_participant set status='C' where id=11");
        AdminFeedbackDto detail = adminFeedbackService.getAdminFeedback(3, "et");
        assertEquals(5, detail.getCriteria().size());
        AdminFeedbackFilterDto filter = new AdminFeedbackFilterDto(); filter.setCourseId("14");
        AdminFeedbackPageDto page = adminFeedbackService.getAdminFeedbackPage(filter);
        assertEquals(6L, page.getTotalElements()); assertEquals(5L, page.getCourseResponseRate().getRegisteredCount());
        assertEquals(5, page.getCriteriaAverages().size());
    }

    @Test
    void actualParticipantChangeInvalidatesReviewButNoOpDoesNot() {
        AdminFeedbackDto detail = adminFeedbackService.getAdminFeedback(3, "et");
        FeedbackRequestDto feedbackRequestDto = requestFrom(detail);
        feedbackService.updateParticipantFeedback(2, 11, feedbackRequestDto);
        assertEquals(detail.getAnswersVersion(), adminFeedbackService.getAdminFeedback(3, "et").getAnswersVersion());
        adminFeedbackService.reviewAdminFeedback(3, detail.getAnswersVersion());
        feedbackRequestDto.getAnswers().getFirst().setScore(6);
        feedbackService.updateParticipantFeedback(2, 11, feedbackRequestDto);
        assertEquals("U", adminFeedbackService.getAdminFeedback(3, "et").getStatus());
        assertThrows(ConflictException.class, () -> adminFeedbackService.reviewAdminFeedback(3, detail.getAnswersVersion()));
    }

    @Test
    void sameDatabaseLockSerializesParticipantAndAdminInBothOrders() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            for (boolean participantFirst : List.of(true, false)) {
                AdminFeedbackDto opened = adminFeedbackService.getAdminFeedback(3, "et");
                FeedbackRequestDto request = requestFrom(opened);
                request.getAnswers().getFirst().setScore(participantFirst ? 6 : 7);
                CountDownLatch waiting = new CountDownLatch(1);
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                Future<?>[] second = new Future<?>[1];
                transactionTemplate.executeWithoutResult(status -> {
                    feedbackRepository.findFeedbackForUpdateBy(3).orElseThrow();
                    second[0] = executor.submit(() -> {
                        waiting.countDown();
                        if (participantFirst) adminFeedbackService.reviewAdminFeedback(3, opened.getAnswersVersion());
                        else feedbackService.updateParticipantFeedback(2, 11, request);
                    });
                    try {
                        assertTrue(waiting.await(2, TimeUnit.SECONDS));
                        assertThrows(TimeoutException.class, () -> second[0].get(150, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException exception) { throw new IllegalStateException(exception); }
                    if (participantFirst) feedbackService.updateParticipantFeedback(2, 11, request);
                    else adminFeedbackService.reviewAdminFeedback(3, opened.getAnswersVersion());
                });
                if (participantFirst) assertInstanceOf(ConflictException.class, assertThrows(ExecutionException.class, () -> second[0].get(5, TimeUnit.SECONDS)).getCause());
                else second[0].get(5, TimeUnit.SECONDS);
                assertEquals("U", adminFeedbackService.getAdminFeedback(3, "et").getStatus());
            }
        } finally { executor.shutdownNow(); }
    }

    private static FeedbackRequestDto requestFrom(AdminFeedbackDto detail) {
        FeedbackRequestDto request = new FeedbackRequestDto();
        request.setAnswers(detail.getCriteria().stream().map(item -> new FeedbackAnswerDto(item.getFeedbackCriteriaId(), item.getScore(), item.getFeedbackText())).toList());
        return request;
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = {FeedbackRepository.class, CourseParticipantFeedbackRepository.class, FeedbackCriteriaRepository.class, FeedbackCriteriaTranslationRepository.class, TrainingTranslationRepository.class})
    @Import({AdminFeedbackService.class, FeedbackService.class, AdminFeedbackRepository.class, FeedbackAnswersVersionService.class, AdminFeedbackMapperImpl.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:adminfeedback;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;DATABASE_TO_LOWER=TRUE", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource); factory.setPackagesToScan("ee.bcskoolitus.persistance");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop", "hibernate.hbm2ddl.create_namespaces", "true", "hibernate.generate_statistics", "true"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) { return new JpaTransactionManager(factory); }
        @Bean Clock trainingClock() { return Clock.fixed(Instant.parse("2026-10-01T09:00:00Z"), ZoneId.of("Europe/Tallinn")); }
        @Bean LanguageService languageService() {
            LanguageService service = mock(LanguageService.class); Language main = new Language(); main.setCode("et");
            when(service.getMainLanguage()).thenReturn(main); return service;
        }
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean TrainingTranslationService trainingTranslationService() { return mock(TrainingTranslationService.class); }
        @Bean CourseService courseService(EntityManagerFactory factory) {
            CourseService service = mock(CourseService.class);
            when(service.getValidCourseBy(anyInt())).thenAnswer(invocation -> {
                EntityManager entityManager = factory.createEntityManager();
                try { return entityManager.find(Course.class, invocation.getArgument(0)); }
                finally { entityManager.close(); }
            });
            return service;
        }
        @Bean CourseParticipantService courseParticipantService(EntityManagerFactory factory) {
            CourseParticipantService service = mock(CourseParticipantService.class);
            when(service.getValidCourseParticipantBy(eq(11))).thenAnswer(invocation -> {
                EntityManager entityManager = factory.createEntityManager();
                try {
                    CourseParticipant courseParticipant = entityManager.find(CourseParticipant.class, 11);
                    courseParticipant.getParticipant().getUser().getId();
                    courseParticipant.getCourse().getEndDate();
                    return courseParticipant;
                } finally { entityManager.close(); }
            });
            return service;
        }
    }
}
