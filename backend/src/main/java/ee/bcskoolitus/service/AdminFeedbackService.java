package ee.bcskoolitus.service;

import ee.bcskoolitus.FeedbackStatus;
import ee.bcskoolitus.controller.adminfeedback.dto.*;
import ee.bcskoolitus.controller.common.dto.FeedbackCriteriaItemDto;
import ee.bcskoolitus.infrastructure.exception.ConflictException;
import ee.bcskoolitus.infrastructure.exception.IncorrectInputException;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedback;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedbackRepository;
import ee.bcskoolitus.persistance.feedback.AdminFeedbackMapper;
import ee.bcskoolitus.persistance.feedback.AdminFeedbackRepository;
import ee.bcskoolitus.persistance.feedback.Feedback;
import ee.bcskoolitus.persistance.feedback.criteria.translation.FeedbackCriteriaTranslation;
import ee.bcskoolitus.persistance.feedback.criteria.translation.FeedbackCriteriaTranslationRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ee.bcskoolitus.Error.FEEDBACK_ANSWERS_CHANGED;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {
    private final AdminFeedbackRepository adminFeedbackRepository;
    private final AdminFeedbackMapper adminFeedbackMapper;
    private final FeedbackService feedbackService;
    private final FeedbackAnswersVersionService feedbackAnswersVersionService;
    private final CourseParticipantFeedbackRepository courseParticipantFeedbackRepository;
    private final FeedbackCriteriaTranslationRepository feedbackCriteriaTranslationRepository;
    private final TrainingTranslationRepository trainingTranslationRepository;
    private final LanguageService languageService;
    private final CourseService courseService;
    private final Clock trainingClock;

    @Transactional(readOnly = true)
    public List<AdminFeedbackCourseDto> getAdminFeedbackCourses(String contentLang) {
        String mainLanguageCode = languageService.getMainLanguage().getCode();
        List<AdminFeedbackCourseDto> adminFeedbackCourseDtos = adminFeedbackRepository.findAdminFeedbackCoursesBy(
                LocalDate.now(trainingClock), resolveContentLanguage(contentLang, mainLanguageCode), mainLanguageCode);
        adminFeedbackCourseDtos.forEach(adminFeedbackCourseDto -> requireTitle(adminFeedbackCourseDto.getTrainingTitle()));
        return adminFeedbackCourseDtos;
    }

    // Leht ja kõik koondid loetakse samast andmebaasi hetktõmmisest.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AdminFeedbackPageDto getAdminFeedbackPage(AdminFeedbackFilterDto adminFeedbackFilterDto) {
        adminFeedbackFilterDto.validate();
        if (adminFeedbackFilterDto.courseIdValue() != null) courseService.getValidCourseBy(adminFeedbackFilterDto.courseIdValue());
        String mainLanguageCode = languageService.getMainLanguage().getCode();
        AdminFeedbackPageDto adminFeedbackPageDto = adminFeedbackRepository.findAdminFeedbackPageBy(adminFeedbackFilterDto,
                LocalDate.now(trainingClock), resolveContentLanguage(adminFeedbackFilterDto.getContentLang(), mainLanguageCode), mainLanguageCode);
        adminFeedbackPageDto.getContent().forEach(adminFeedbackSummaryDto -> requireTitle(adminFeedbackSummaryDto.getTrainingTitle()));
        adminFeedbackPageDto.getCriteriaAverages().forEach(feedbackCriteriaSummaryDto -> requireTitle(feedbackCriteriaSummaryDto.getTitle()));
        return adminFeedbackPageDto;
    }

    // Sama päise lukk kui review ja osaleja PUT-il: vastuste tekst ja versioon on kooskõlas.
    @Transactional
    public AdminFeedbackDto getAdminFeedback(Integer feedbackId, String contentLang) {
        Feedback feedback = feedbackService.getValidFeedbackForUpdateBy(feedbackId);
        List<CourseParticipantFeedback> answers = courseParticipantFeedbackRepository.findAllByFeedbackId(feedbackId);
        String mainLanguageCode = languageService.getMainLanguage().getCode();
        String languageCode = resolveContentLanguage(contentLang, mainLanguageCode);
        AdminFeedbackDto adminFeedbackDto = adminFeedbackMapper.toAdminFeedbackDto(feedback);
        adminFeedbackDto.setTrainingTitle(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(adminFeedbackDto.getTrainingId(), languageCode)
                .or(() -> trainingTranslationRepository.findByTraining_IdAndLanguage_Code(adminFeedbackDto.getTrainingId(), mainLanguageCode))
                .map(TrainingTranslation::getTitle).orElseThrow(() -> new IllegalStateException("Koolituse põhikeelne tõlge puudub")));
        adminFeedbackDto.setAnswersUpdatedAt(answers.stream().map(CourseParticipantFeedback::getUpdatedAt).max(Comparator.naturalOrder()).orElse(null));
        adminFeedbackDto.setAnswersVersion(feedbackAnswersVersionService.getAnswersVersion(feedbackId, answers));
        adminFeedbackDto.setCriteria(getAnsweredCriteria(answers, languageCode, mainLanguageCode));
        return adminFeedbackDto;
    }

    @Transactional
    public void reviewAdminFeedback(Integer feedbackId, String answersVersion) {
        if (answersVersion == null || !answersVersion.matches("[0-9a-f]{64}")) throw new IncorrectInputException("X-Answers-Version");
        Feedback feedback = feedbackService.getValidFeedbackForUpdateBy(feedbackId);
        List<CourseParticipantFeedback> answers = courseParticipantFeedbackRepository.findAllByFeedbackId(feedbackId);
        if (!answersVersion.equals(feedbackAnswersVersionService.getAnswersVersion(feedbackId, answers))) {
            throw new ConflictException(FEEDBACK_ANSWERS_CHANGED.getMessage(), FEEDBACK_ANSWERS_CHANGED.name());
        }
        if (!FeedbackStatus.HISTORICAL.getCode().equals(feedback.getStatus())) feedback.setStatus(FeedbackStatus.HISTORICAL.getCode());
    }

    private List<FeedbackCriteriaItemDto> getAnsweredCriteria(List<CourseParticipantFeedback> answers, String contentLang, String mainLang) {
        if (answers.isEmpty()) return List.of();
        List<Integer> criteriaIds = answers.stream().map(answer -> answer.getFeedbackCriteria().getId()).toList();
        Map<Integer, FeedbackCriteriaTranslation> mainTranslations = new HashMap<>();
        Map<Integer, FeedbackCriteriaTranslation> contentTranslations = new HashMap<>();
        for (FeedbackCriteriaTranslation translation : feedbackCriteriaTranslationRepository.findFeedbackCriteriaTranslationsBy(criteriaIds, List.of(contentLang, mainLang))) {
            if (translation.getLanguage().getCode().equals(mainLang)) mainTranslations.put(translation.getFeedbackCriteria().getId(), translation);
            if (translation.getLanguage().getCode().equals(contentLang)) contentTranslations.put(translation.getFeedbackCriteria().getId(), translation);
        }
        return answers.stream().sorted(Comparator.comparing((CourseParticipantFeedback answer) -> answer.getFeedbackCriteria().getSequence())
                .thenComparing(answer -> answer.getFeedbackCriteria().getId())).map(answer -> {
            FeedbackCriteriaTranslation translation = contentTranslations.getOrDefault(answer.getFeedbackCriteria().getId(), mainTranslations.get(answer.getFeedbackCriteria().getId()));
            if (translation == null) throw new IllegalStateException("Kriteeriumi põhikeelne tõlge puudub");
            return new FeedbackCriteriaItemDto(answer.getFeedbackCriteria().getId(), translation.getTitle(), translation.getDescription(), answer.getScore(), answer.getFeedbackText());
        }).toList();
    }

    private static String resolveContentLanguage(String contentLang, String mainLanguageCode) {
        return contentLang == null || contentLang.isBlank() ? mainLanguageCode : contentLang;
    }
    private static void requireTitle(String title) {
        if (title == null) throw new IllegalStateException("Põhikeelne tõlge puudub");
    }
}
