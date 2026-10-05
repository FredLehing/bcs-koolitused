package ee.bcskoolitus.service;

import ee.bcskoolitus.FeedbackCriteriaStatus;
import ee.bcskoolitus.FeedbackStatus;
import ee.bcskoolitus.controller.feedback.dto.FeedbackAnswerDto;
import ee.bcskoolitus.controller.common.dto.FeedbackCriteriaItemDto;
import ee.bcskoolitus.controller.feedback.dto.FeedbackRequestDto;
import ee.bcskoolitus.controller.feedback.dto.ParticipantFeedbackDto;
import ee.bcskoolitus.infrastructure.exception.DataNotFoundException;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ee.bcskoolitus.Error.FEEDBACK_ALREADY_EXISTS;
import static ee.bcskoolitus.Error.FEEDBACK_CRITERIA_CHANGED;
import static ee.bcskoolitus.Error.FEEDBACK_NOT_ALLOWED;
import static ee.bcskoolitus.Error.FEEDBACK_NOT_FOUND;
import static ee.bcskoolitus.Error.REGISTRATION_NOT_FOUND;

// Osaleja tagasiside oma registreerumise kohta: vorm (GET), lisamine (POST) ja muutmine (PUT).
// Otsused: docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-skeemid.md
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CourseParticipantFeedbackRepository courseParticipantFeedbackRepository;
    private final FeedbackCriteriaRepository feedbackCriteriaRepository;
    private final FeedbackCriteriaTranslationRepository feedbackCriteriaTranslationRepository;
    private final CourseParticipantService courseParticipantService;
    private final UserService userService;
    private final TrainingTranslationService trainingTranslationService;
    private final LanguageService languageService;

    public Feedback getValidFeedbackForUpdateBy(Integer feedbackId) {
        return feedbackRepository.findFeedbackForUpdateBy(feedbackId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("feedbackId", feedbackId));
    }

    // Uus tagasiside: aktiivsed kriteeriumid ilma vastusteta; olemasolev: aktiivsed + kustutatud, millele on vastatud
    @Transactional(readOnly = true)
    public ParticipantFeedbackDto getParticipantFeedback(Integer userId, Integer courseParticipantId, String contentLang) {
        CourseParticipant courseParticipant = getValidFeedbackCourseParticipantBy(userId, courseParticipantId);
        Optional<Feedback> feedback = feedbackRepository.findByCourseParticipantId(courseParticipantId);
        List<CourseParticipantFeedback> answers = feedback
                .map(existingFeedback -> courseParticipantFeedbackRepository.findAllByFeedbackId(existingFeedback.getId()))
                .orElse(List.of());
        Course course = courseParticipant.getCourse();
        ParticipantFeedbackDto participantFeedbackDto = new ParticipantFeedbackDto();
        participantFeedbackDto.setCourseParticipantId(courseParticipantId);
        participantFeedbackDto.setTrainingTitle(trainingTranslationService.getTrainingTitle(course.getTraining().getId(), contentLang));
        participantFeedbackDto.setStartDate(course.getStartDate());
        participantFeedbackDto.setEndDate(course.getEndDate());
        participantFeedbackDto.setHasFeedback(feedback.isPresent());
        participantFeedbackDto.setCreatedAt(feedback.map(Feedback::getCreatedAt).orElse(null));
        participantFeedbackDto.setUpdatedAt(findAnswersUpdatedAt(answers));
        participantFeedbackDto.setCriteria(createFeedbackCriteriaItemDtos(findFormCriteria(answers), answers, contentLang));
        return participantFeedbackDto;
    }

    // Esimene tagasiside: feedback (N) + vastus iga aktiivse kriteeriumi kohta
    @Transactional
    public void addParticipantFeedback(Integer userId, Integer courseParticipantId, FeedbackRequestDto feedbackRequestDto) {
        CourseParticipant courseParticipant = getValidFeedbackCourseParticipantBy(userId, courseParticipantId);
        if (feedbackRepository.findByCourseParticipantId(courseParticipantId).isPresent()) {
            throw new ForbiddenException(FEEDBACK_ALREADY_EXISTS.getMessage(), FEEDBACK_ALREADY_EXISTS.name());
        }
        List<FeedbackCriteria> formCriteria = findFormCriteria(List.of());
        validateAnswersMatchCriteria(feedbackRequestDto.getAnswers(), formCriteria);
        Feedback feedback = new Feedback();
        feedback.setCourseParticipant(courseParticipant);
        feedback.setStatus(FeedbackStatus.NEW.getCode());
        feedbackRepository.save(feedback);
        Map<Integer, FeedbackCriteria> feedbackCriteriaById = toFeedbackCriteriaById(formCriteria);
        List<CourseParticipantFeedback> answers = feedbackRequestDto.getAnswers().stream()
                .map(feedbackAnswerDto -> createAnswer(feedback, feedbackCriteriaById.get(feedbackAnswerDto.getFeedbackCriteriaId()), feedbackAnswerDto))
                .toList();
        courseParticipantFeedbackRepository.saveAll(answers);
    }

    // Muutmine: vastused uuenevad, hiljem lisatud kriteeriumi vastus lisatakse; staatus H → U (N ja U jäävad)
    @Transactional
    public void updateParticipantFeedback(Integer userId, Integer courseParticipantId, FeedbackRequestDto feedbackRequestDto) {
        getValidFeedbackCourseParticipantBy(userId, courseParticipantId);
        Feedback feedback = feedbackRepository.findFeedbackForUpdateByCourseParticipantId(courseParticipantId)
                .orElseThrow(() -> new DataNotFoundException(FEEDBACK_NOT_FOUND.getMessage(), FEEDBACK_NOT_FOUND.name()));
        List<CourseParticipantFeedback> answers = courseParticipantFeedbackRepository.findAllByFeedbackId(feedback.getId());
        List<FeedbackCriteria> formCriteria = findFormCriteria(answers);
        validateAnswersMatchCriteria(feedbackRequestDto.getAnswers(), formCriteria);
        Map<Integer, FeedbackCriteria> feedbackCriteriaById = toFeedbackCriteriaById(formCriteria);
        Map<Integer, CourseParticipantFeedback> answerByFeedbackCriteriaId = answers.stream()
                .collect(Collectors.toMap(answer -> answer.getFeedbackCriteria().getId(), Function.identity()));
        List<CourseParticipantFeedback> changedAnswers = new ArrayList<>();
        for (FeedbackAnswerDto feedbackAnswerDto : feedbackRequestDto.getAnswers()) {
            CourseParticipantFeedback answer = answerByFeedbackCriteriaId.get(feedbackAnswerDto.getFeedbackCriteriaId());
            if (answer == null) {
                changedAnswers.add(createAnswer(feedback, feedbackCriteriaById.get(feedbackAnswerDto.getFeedbackCriteriaId()), feedbackAnswerDto));
            } else if (handleUpdateAnswer(answer, feedbackAnswerDto)) {
                changedAnswers.add(answer);
            }
        }
        if (changedAnswers.isEmpty()) {
            return;
        }
        courseParticipantFeedbackRepository.saveAll(changedAnswers);
        if (FeedbackStatus.HISTORICAL.getCode().equals(feedback.getStatus())) {
            feedback.setStatus(FeedbackStatus.UPDATED.getCode());
            feedbackRepository.save(feedback);
        }
    }

    // Registreerumine on olemas, kuulub kasutajale ja tagasiside andmine on lubatud (sama reegel mis canGiveFeedback)
    private CourseParticipant getValidFeedbackCourseParticipantBy(Integer userId, Integer courseParticipantId) {
        userService.getValidUserBy(userId);
        CourseParticipant courseParticipant = courseParticipantService.getValidCourseParticipantBy(courseParticipantId);
        if (!courseParticipant.getParticipant().getUser().getId().equals(userId)) {
            throw new DataNotFoundException(REGISTRATION_NOT_FOUND.getMessage(), REGISTRATION_NOT_FOUND.name());
        }
        if (!CourseParticipantService.isFeedbackAllowed(courseParticipant)) {
            throw new ForbiddenException(FEEDBACK_NOT_ALLOWED.getMessage(), FEEDBACK_NOT_ALLOWED.name());
        }
        return courseParticipant;
    }

    // Vormi kriteeriumid järjekorras: aktiivsed + kustutatud, millele on vastatud
    private List<FeedbackCriteria> findFormCriteria(List<CourseParticipantFeedback> answers) {
        Set<Integer> answeredFeedbackCriteriaIds = answers.stream()
                .map(answer -> answer.getFeedbackCriteria().getId())
                .collect(Collectors.toSet());
        return feedbackCriteriaRepository.findAllByOrderBySequenceAscIdAsc().stream()
                .filter(feedbackCriteria -> FeedbackCriteriaStatus.ACTIVE.getCode().equals(feedbackCriteria.getStatus())
                        || answeredFeedbackCriteriaIds.contains(feedbackCriteria.getId()))
                .toList();
    }

    // Iga vormi kriteerium täpselt üks kord; muidu on kriteeriumid vahepeal muutunud
    private static void validateAnswersMatchCriteria(List<FeedbackAnswerDto> feedbackAnswerDtos, List<FeedbackCriteria> formCriteria) {
        List<Integer> answeredFeedbackCriteriaIds = feedbackAnswerDtos.stream().map(FeedbackAnswerDto::getFeedbackCriteriaId).toList();
        Set<Integer> formFeedbackCriteriaIds = formCriteria.stream().map(FeedbackCriteria::getId).collect(Collectors.toSet());
        boolean hasDuplicates = Set.copyOf(answeredFeedbackCriteriaIds).size() != answeredFeedbackCriteriaIds.size();
        if (hasDuplicates || !Set.copyOf(answeredFeedbackCriteriaIds).equals(formFeedbackCriteriaIds)) {
            throw new ForbiddenException(FEEDBACK_CRITERIA_CHANGED.getMessage(), FEEDBACK_CRITERIA_CHANGED.name());
        }
    }

    private static Map<Integer, FeedbackCriteria> toFeedbackCriteriaById(List<FeedbackCriteria> formCriteria) {
        return formCriteria.stream().collect(Collectors.toMap(FeedbackCriteria::getId, Function.identity()));
    }

    private static CourseParticipantFeedback createAnswer(Feedback feedback, FeedbackCriteria feedbackCriteria, FeedbackAnswerDto feedbackAnswerDto) {
        CourseParticipantFeedback answer = new CourseParticipantFeedback();
        answer.setFeedback(feedback);
        answer.setFeedbackCriteria(feedbackCriteria);
        answer.setScore(feedbackAnswerDto.getScore());
        answer.setFeedbackText(trimToNull(feedbackAnswerDto.getFeedbackText()));
        return answer;
    }

    // true, kui hinne või kommentaar muutus
    private static boolean handleUpdateAnswer(CourseParticipantFeedback answer, FeedbackAnswerDto feedbackAnswerDto) {
        String feedbackText = trimToNull(feedbackAnswerDto.getFeedbackText());
        if (answer.getScore().equals(feedbackAnswerDto.getScore()) && Objects.equals(answer.getFeedbackText(), feedbackText)) {
            return false;
        }
        answer.setScore(feedbackAnswerDto.getScore());
        answer.setFeedbackText(feedbackText);
        return true;
    }

    private static Instant findAnswersUpdatedAt(List<CourseParticipantFeedback> answers) {
        return answers.stream()
                .map(CourseParticipantFeedback::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    // title ja description contentLang keeles, tõlke puudumisel põhikeeles
    private List<FeedbackCriteriaItemDto> createFeedbackCriteriaItemDtos(List<FeedbackCriteria> formCriteria,
                                                                       List<CourseParticipantFeedback> answers, String contentLang) {
        if (formCriteria.isEmpty()) {
            return List.of();
        }
        String mainLanguageCode = languageService.getMainLanguage().getCode();
        List<Integer> feedbackCriteriaIds = formCriteria.stream().map(FeedbackCriteria::getId).toList();
        Map<Integer, FeedbackCriteriaTranslation> contentTranslations = new HashMap<>();
        Map<Integer, FeedbackCriteriaTranslation> mainTranslations = new HashMap<>();
        for (FeedbackCriteriaTranslation translation : feedbackCriteriaTranslationRepository.findFeedbackCriteriaTranslationsBy(
                feedbackCriteriaIds, List.of(contentLang, mainLanguageCode))) {
            Integer feedbackCriteriaId = translation.getFeedbackCriteria().getId();
            if (translation.getLanguage().getCode().equals(contentLang)) {
                contentTranslations.put(feedbackCriteriaId, translation);
            }
            if (translation.getLanguage().getCode().equals(mainLanguageCode)) {
                mainTranslations.put(feedbackCriteriaId, translation);
            }
        }
        Map<Integer, CourseParticipantFeedback> answerByFeedbackCriteriaId = answers.stream()
                .collect(Collectors.toMap(answer -> answer.getFeedbackCriteria().getId(), Function.identity()));
        return formCriteria.stream()
                .map(feedbackCriteria -> {
                    FeedbackCriteriaTranslation translation = contentTranslations.getOrDefault(
                            feedbackCriteria.getId(), mainTranslations.get(feedbackCriteria.getId()));
                    CourseParticipantFeedback answer = answerByFeedbackCriteriaId.get(feedbackCriteria.getId());
                    return new FeedbackCriteriaItemDto(
                            feedbackCriteria.getId(),
                            translation == null ? "" : translation.getTitle(),
                            translation == null ? "" : translation.getDescription(),
                            answer == null ? null : answer.getScore(),
                            answer == null ? null : answer.getFeedbackText());
                })
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
