package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.training.dto.TrainingPageDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslation;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationRepository;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingPageMapper;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturer;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingPageService {
    private final TrainingService trainingService;
    private final TrainingTranslationRepository trainingTranslationRepository;
    private final LanguageService languageService;
    private final TrainingPageMapper trainingPageMapper;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;
    private final FundingTypeTranslationMapper fundingTypeTranslationMapper;
    private final TrainingLecturerRepository trainingLecturerRepository;
    private final LecturerService lecturerService;
    private final PublicCourseSummaryRepository publicCourseSummaryRepository;
    private final PublicCourseSummaryMapper publicCourseSummaryMapper;
    private final TrainingTranslationCurriculumService trainingTranslationCurriculumService;

    // Mustand jääb vormi eelvaates avatavaks nagu senises TrainingView-s; kustutatu on 404.
    @Transactional(readOnly = true)
    public TrainingPageDto getTrainingPage(Integer trainingId, String contentLang, Integer trainingTranslationId) {
        Training training = trainingService.getValidActiveTrainingBy(trainingId);
        Optional<TrainingTranslation> requestedTranslation = trainingTranslationId == null ? Optional.empty()
                : trainingTranslationRepository.findTrainingTranslationBy(trainingTranslationId, trainingId);
        TrainingTranslation trainingTranslation = requestedTranslation
                .or(() -> trainingTranslationRepository.findByTraining_IdAndLanguage_Code(trainingId, contentLang))
                .or(() -> trainingTranslationRepository.findByTraining_IdAndLanguage_Code(trainingId, languageService.getMainLanguage().getCode()))
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingId", trainingId));
        String displayedLanguageCode = trainingTranslation.getLanguage().getCode();
        boolean isMainLanguageFallback = requestedTranslation.isEmpty() && !displayedLanguageCode.equals(contentLang);
        TrainingPageDto trainingPageDto = trainingPageMapper.toTrainingPageDto(training, trainingTranslation);
        trainingPageDto.setIsMainLanguageFallback(isMainLanguageFallback);
        trainingPageDto.setCategoryName(categoryTranslationRepository
                .findByCategory_IdAndLanguage_Id(training.getCategory().getId(), trainingTranslation.getLanguage().getId())
                .map(CategoryTranslation::getName).orElse(null));
        trainingPageDto.setFundingTypes(fundingTypeTranslationMapper.toFundingTypeDtos(
                fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(trainingId, displayedLanguageCode)));
        trainingPageDto.setLecturers(lecturerService.findLecturerSummariesBy(
                trainingLecturerRepository.findTrainingLecturersBy(trainingId).stream()
                        .map(TrainingLecturer::getLecturer).toList(), contentLang));
        trainingPageDto.setUpcomingCourses(publicCourseSummaryMapper.toUpcomingCourseDtos(
                publicCourseSummaryRepository.findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(trainingId, displayedLanguageCode)));
        if (!isMainLanguageFallback) handleAddCurriculumInfo(trainingPageDto);
        return trainingPageDto;
    }

    private void handleAddCurriculumInfo(TrainingPageDto trainingPageDto) {
        trainingTranslationCurriculumService.findCurriculumInfo(trainingPageDto.getTrainingTranslationId())
                .ifPresent(trainingTranslationCurriculumInfo -> {
                    trainingPageDto.setCurriculumFileName(trainingTranslationCurriculumInfo.getFileName());
                    trainingPageDto.setCurriculumFileSize(trainingTranslationCurriculumInfo.getFileSize());
                });
    }
}
