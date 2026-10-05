package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.feedback.dto.FeedbackAnswerDto;
import ee.bcskoolitus.controller.common.dto.FeedbackCriteriaItemDto;
import ee.bcskoolitus.controller.feedback.dto.FeedbackRequestDto;
import ee.bcskoolitus.controller.feedback.dto.ParticipantFeedbackDto;
import ee.bcskoolitus.infrastructure.exception.DataNotFoundException;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedback;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedbackRepository;
import ee.bcskoolitus.persistance.feedback.Feedback;
import ee.bcskoolitus.persistance.feedback.FeedbackRepository;
import ee.bcskoolitus.persistance.feedback.criteria.FeedbackCriteria;
import ee.bcskoolitus.persistance.feedback.criteria.FeedbackCriteriaRepository;
import ee.bcskoolitus.persistance.feedback.criteria.translation.FeedbackCriteriaTranslation;
import ee.bcskoolitus.persistance.feedback.criteria.translation.FeedbackCriteriaTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedbackServiceTest {

    private static final int USER_ID = 2;

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private CourseParticipantFeedbackRepository courseParticipantFeedbackRepository;
    @Mock
    private FeedbackCriteriaRepository feedbackCriteriaRepository;
    @Mock
    private FeedbackCriteriaTranslationRepository feedbackCriteriaTranslationRepository;
    @Mock
    private CourseParticipantService courseParticipantService;
    @Mock
    private UserService userService;
    @Mock
    private TrainingTranslationService trainingTranslationService;
    @Mock
    private LanguageService languageService;

    @InjectMocks
    private FeedbackService feedbackService;

    // ---------- GET ----------

    @Test
    void getParticipantFeedback_withoutFeedback_returnsActiveCriteriaWithoutAnswers() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        givenCriteria(criteria(1, 1, "A"), criteria(2, 2, "A"), criteria(3, 3, "D"));
        when(feedbackRepository.findByCourseParticipantId(2)).thenReturn(Optional.empty());

        ParticipantFeedbackDto participantFeedbackDto = feedbackService.getParticipantFeedback(USER_ID, 2, "et");

        assertFalse(participantFeedbackDto.getHasFeedback());
        assertNull(participantFeedbackDto.getCreatedAt());
        assertNull(participantFeedbackDto.getUpdatedAt());
        assertEquals("Java algkursus", participantFeedbackDto.getTrainingTitle());
        assertEquals(List.of(1, 2), participantFeedbackDto.getCriteria().stream().map(FeedbackCriteriaItemDto::getFeedbackCriteriaId).toList());
        assertNull(participantFeedbackDto.getCriteria().getFirst().getScore());
        assertEquals("Kriteerium 1 et", participantFeedbackDto.getCriteria().getFirst().getTitle());
    }

    @Test
    void getParticipantFeedback_withFeedback_includesAnsweredDeletedAndNewCriteria() {
        givenCourseParticipant(10, "R", LocalDate.now().minusDays(100), "O");
        FeedbackCriteria deleted = criteria(2, 2, "D");
        givenCriteria(criteria(1, 1, "A"), deleted, criteria(3, 3, "D"), criteria(4, 4, "A"));
        Feedback feedback = feedback("H");
        when(feedbackRepository.findByCourseParticipantId(10)).thenReturn(Optional.of(feedback));
        CourseParticipantFeedback first = answer(feedback, criteria(1, 1, "A"), 9, null);
        first.setUpdatedAt(Instant.parse("2026-06-12T13:40:00Z"));
        CourseParticipantFeedback second = answer(feedback, deleted, 7, "Kommentaar");
        second.setUpdatedAt(Instant.parse("2026-07-01T10:00:00Z"));
        when(courseParticipantFeedbackRepository.findAllByFeedbackId(1)).thenReturn(List.of(first, second));

        ParticipantFeedbackDto participantFeedbackDto = feedbackService.getParticipantFeedback(USER_ID, 10, "et");

        assertTrue(participantFeedbackDto.getHasFeedback());
        assertEquals(Instant.parse("2026-06-12T13:40:00Z"), participantFeedbackDto.getCreatedAt());
        assertEquals(Instant.parse("2026-07-01T10:00:00Z"), participantFeedbackDto.getUpdatedAt());
        List<FeedbackCriteriaItemDto> criteria = participantFeedbackDto.getCriteria();
        assertEquals(List.of(1, 2, 4), criteria.stream().map(FeedbackCriteriaItemDto::getFeedbackCriteriaId).toList());
        assertEquals(7, criteria.get(1).getScore());
        assertEquals("Kommentaar", criteria.get(1).getFeedbackText());
        assertNull(criteria.get(2).getScore());
    }

    @Test
    void getParticipantFeedback_missingTranslation_usesMainLanguage() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        givenCriteria(criteria(1, 1, "A"));
        when(feedbackRepository.findByCourseParticipantId(2)).thenReturn(Optional.empty());

        ParticipantFeedbackDto participantFeedbackDto = feedbackService.getParticipantFeedback(USER_ID, 2, "ru");

        assertEquals("Kriteerium 1 et", participantFeedbackDto.getCriteria().getFirst().getTitle());
    }

    @Test
    void getParticipantFeedback_otherUsersRegistration_throwsRegistrationNotFound() {
        CourseParticipant courseParticipant = givenCourseParticipant(3, "R", LocalDate.now().minusDays(20), "O");
        courseParticipant.getParticipant().getUser().setId(5);

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> feedbackService.getParticipantFeedback(USER_ID, 3, "et"));
        assertEquals("REGISTRATION_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getParticipantFeedback_notAllowed_throwsFeedbackNotAllowed() {
        givenCourseParticipant(1, "R", LocalDate.now().plusDays(5), "O");
        assertFeedbackNotAllowed(1);
        givenCourseParticipant(4, "C", LocalDate.now().minusDays(5), "O");
        assertFeedbackNotAllowed(4);
        givenCourseParticipant(5, "R", LocalDate.now().minusDays(5), "X");
        assertFeedbackNotAllowed(5);
    }

    @Test
    void getParticipantFeedback_lastCourseDay_isAllowed() {
        givenCourseParticipant(2, "R", LocalDate.now(), "O");
        givenCriteria(criteria(1, 1, "A"));
        when(feedbackRepository.findByCourseParticipantId(2)).thenReturn(Optional.empty());

        assertFalse(feedbackService.getParticipantFeedback(USER_ID, 2, "et").getHasFeedback());
    }

    // ---------- POST ----------

    @Test
    void addParticipantFeedback_createsNewFeedbackAndAnswers() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        givenCriteria(criteria(1, 1, "A"), criteria(2, 2, "A"), criteria(3, 3, "D"));
        when(feedbackRepository.findByCourseParticipantId(2)).thenReturn(Optional.empty());

        feedbackService.addParticipantFeedback(USER_ID, 2, request(new FeedbackAnswerDto(1, 9, "  Hea  "), new FeedbackAnswerDto(2, 10, "   ")));

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(feedbackCaptor.capture());
        assertEquals("N", feedbackCaptor.getValue().getStatus());
        List<CourseParticipantFeedback> answers = captureSavedAnswers();
        assertEquals(2, answers.size());
        assertEquals("Hea", answers.get(0).getFeedbackText());
        assertNull(answers.get(1).getFeedbackText());
        assertEquals(10, answers.get(1).getScore());
    }

    @Test
    void addParticipantFeedback_existingFeedback_throwsAlreadyExists() {
        givenCourseParticipant(10, "R", LocalDate.now().minusDays(100), "O");
        when(feedbackRepository.findByCourseParticipantId(10)).thenReturn(Optional.of(feedback("H")));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> feedbackService.addParticipantFeedback(USER_ID, 10, request(new FeedbackAnswerDto(1, 9, null))));
        assertEquals("FEEDBACK_ALREADY_EXISTS", exception.getErrorCode());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void addParticipantFeedback_missingOrDuplicateCriteria_throwsCriteriaChanged() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        givenCriteria(criteria(1, 1, "A"), criteria(2, 2, "A"));
        when(feedbackRepository.findByCourseParticipantId(2)).thenReturn(Optional.empty());

        assertCriteriaChanged(() -> feedbackService.addParticipantFeedback(USER_ID, 2, request(new FeedbackAnswerDto(1, 9, null))));
        assertCriteriaChanged(() -> feedbackService.addParticipantFeedback(USER_ID, 2,
                request(new FeedbackAnswerDto(1, 9, null), new FeedbackAnswerDto(1, 8, null), new FeedbackAnswerDto(2, 8, null))));
        assertCriteriaChanged(() -> feedbackService.addParticipantFeedback(USER_ID, 2,
                request(new FeedbackAnswerDto(1, 9, null), new FeedbackAnswerDto(2, 8, null), new FeedbackAnswerDto(99, 8, null))));
        verify(feedbackRepository, never()).save(any());
    }

    // ---------- PUT ----------

    @Test
    void updateParticipantFeedback_historicalBecomesUpdated_andNewCriteriaIsAdded() {
        givenCourseParticipant(10, "R", LocalDate.now().minusDays(100), "O");
        FeedbackCriteria first = criteria(1, 1, "A");
        givenCriteria(first, criteria(2, 2, "A"));
        Feedback feedback = feedback("H");
        when(feedbackRepository.findFeedbackForUpdateByCourseParticipantId(10)).thenReturn(Optional.of(feedback));
        CourseParticipantFeedback existing = answer(feedback, first, 9, null);
        when(courseParticipantFeedbackRepository.findAllByFeedbackId(1)).thenReturn(List.of(existing));

        feedbackService.updateParticipantFeedback(USER_ID, 10, request(new FeedbackAnswerDto(1, 6, "Muutsin"), new FeedbackAnswerDto(2, 8, null)));

        assertEquals(6, existing.getScore());
        assertEquals("Muutsin", existing.getFeedbackText());
        assertEquals(2, captureSavedAnswers().size());
        assertEquals("U", feedback.getStatus());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void updateParticipantFeedback_newStatusStaysNew() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        FeedbackCriteria first = criteria(1, 1, "A");
        givenCriteria(first);
        Feedback feedback = feedback("N");
        when(feedbackRepository.findFeedbackForUpdateByCourseParticipantId(2)).thenReturn(Optional.of(feedback));
        when(courseParticipantFeedbackRepository.findAllByFeedbackId(1)).thenReturn(List.of(answer(feedback, first, 9, null)));

        feedbackService.updateParticipantFeedback(USER_ID, 2, request(new FeedbackAnswerDto(1, 3, null)));

        assertEquals("N", feedback.getStatus());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void updateParticipantFeedback_answeredDeletedCriteriaIsRequired() {
        givenCourseParticipant(10, "R", LocalDate.now().minusDays(100), "O");
        FeedbackCriteria deleted = criteria(2, 2, "D");
        givenCriteria(criteria(1, 1, "A"), deleted);
        Feedback feedback = feedback("H");
        when(feedbackRepository.findFeedbackForUpdateByCourseParticipantId(10)).thenReturn(Optional.of(feedback));
        when(courseParticipantFeedbackRepository.findAllByFeedbackId(1))
                .thenReturn(List.of(answer(feedback, criteria(1, 1, "A"), 9, null), answer(feedback, deleted, 7, null)));

        assertCriteriaChanged(() -> feedbackService.updateParticipantFeedback(USER_ID, 10, request(new FeedbackAnswerDto(1, 9, null))));
    }

    @Test
    void updateParticipantFeedback_withoutFeedback_throwsFeedbackNotFound() {
        givenCourseParticipant(2, "R", LocalDate.now().minusDays(20), "O");
        when(feedbackRepository.findFeedbackForUpdateByCourseParticipantId(2)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> feedbackService.updateParticipantFeedback(USER_ID, 2, request(new FeedbackAnswerDto(1, 9, null))));
        assertEquals("FEEDBACK_NOT_FOUND", exception.getErrorCode());
    }

    // ---------- abi ----------

    private void assertFeedbackNotAllowed(Integer courseParticipantId) {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> feedbackService.getParticipantFeedback(USER_ID, courseParticipantId, "et"));
        assertEquals("FEEDBACK_NOT_ALLOWED", exception.getErrorCode());
    }

    private static void assertCriteriaChanged(org.junit.jupiter.api.function.Executable executable) {
        ForbiddenException exception = assertThrows(ForbiddenException.class, executable);
        assertEquals("FEEDBACK_CRITERIA_CHANGED", exception.getErrorCode());
    }

    @SuppressWarnings("unchecked")
    private List<CourseParticipantFeedback> captureSavedAnswers() {
        ArgumentCaptor<List<CourseParticipantFeedback>> captor = ArgumentCaptor.forClass(List.class);
        verify(courseParticipantFeedbackRepository).saveAll(captor.capture());
        return new ArrayList<>(captor.getValue());
    }

    private CourseParticipant givenCourseParticipant(Integer courseParticipantId, String status, LocalDate endDate, String courseStatus) {
        Training training = new Training();
        training.setId(1);
        Course course = new Course();
        course.setTraining(training);
        course.setStartDate(endDate.minusDays(4));
        course.setEndDate(endDate);
        course.setStatus(courseStatus);
        User user = new User();
        user.setId(USER_ID);
        Participant participant = new Participant();
        participant.setId(1);
        participant.setUser(user);
        CourseParticipant courseParticipant = new CourseParticipant();
        courseParticipant.setId(courseParticipantId);
        courseParticipant.setCourse(course);
        courseParticipant.setParticipant(participant);
        courseParticipant.setStatus(status);
        when(courseParticipantService.getValidCourseParticipantBy(courseParticipantId)).thenReturn(courseParticipant);
        when(trainingTranslationService.getTrainingTitle(1, "et")).thenReturn("Java algkursus");
        Language mainLanguage = language("et");
        when(languageService.getMainLanguage()).thenReturn(mainLanguage);
        return courseParticipant;
    }

    // Igal kriteeriumil ainult põhikeele (et) tõlge
    private void givenCriteria(FeedbackCriteria... feedbackCriteria) {
        when(feedbackCriteriaRepository.findAllByOrderBySequenceAscIdAsc()).thenReturn(List.of(feedbackCriteria));
        List<FeedbackCriteriaTranslation> translations = new ArrayList<>();
        for (FeedbackCriteria criteria : feedbackCriteria) {
            FeedbackCriteriaTranslation translation = new FeedbackCriteriaTranslation();
            translation.setFeedbackCriteria(criteria);
            translation.setLanguage(language("et"));
            translation.setTitle("Kriteerium " + criteria.getId() + " et");
            translation.setDescription("Kirjeldus " + criteria.getId());
            translations.add(translation);
        }
        when(feedbackCriteriaTranslationRepository.findFeedbackCriteriaTranslationsBy(anyList(), anyList())).thenReturn(translations);
    }

    private static FeedbackCriteria criteria(Integer id, Integer sequence, String status) {
        FeedbackCriteria feedbackCriteria = new FeedbackCriteria();
        feedbackCriteria.setId(id);
        feedbackCriteria.setSequence(sequence);
        feedbackCriteria.setStatus(status);
        return feedbackCriteria;
    }

    private static Feedback feedback(String status) {
        Feedback feedback = new Feedback();
        feedback.setId(1);
        feedback.setStatus(status);
        feedback.setCreatedAt(Instant.parse("2026-06-12T13:40:00Z"));
        return feedback;
    }

    private static CourseParticipantFeedback answer(Feedback feedback, FeedbackCriteria feedbackCriteria, Integer score, String feedbackText) {
        CourseParticipantFeedback answer = new CourseParticipantFeedback();
        answer.setFeedback(feedback);
        answer.setFeedbackCriteria(feedbackCriteria);
        answer.setScore(score);
        answer.setFeedbackText(feedbackText);
        return answer;
    }

    private static Language language(String code) {
        Language language = new Language();
        language.setCode(code);
        return language;
    }

    private static FeedbackRequestDto request(FeedbackAnswerDto... feedbackAnswerDtos) {
        return new FeedbackRequestDto(List.of(feedbackAnswerDtos));
    }
}
