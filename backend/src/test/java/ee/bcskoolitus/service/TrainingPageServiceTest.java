package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.training.dto.TrainingPageDto;
import ee.bcskoolitus.controller.common.dto.LecturerSummaryDto;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturer;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationRepository;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapperImpl;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.location.Location;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingPageMapper;
import ee.bcskoolitus.persistance.training.TrainingPageMapperImpl;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculumInfo;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummary;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPageServiceTest {
    @Mock private TrainingService trainingService;
    @Mock private TrainingTranslationRepository trainingTranslationRepository;
    @Mock private LanguageService languageService;
    @Mock private CategoryTranslationRepository categoryTranslationRepository;
    @Mock private FundingTypeTranslationRepository fundingTypeTranslationRepository;
    @Mock private TrainingLecturerRepository trainingLecturerRepository;
    @Mock private PublicCourseSummaryRepository publicCourseSummaryRepository;
    @Mock private TrainingTranslationCurriculumService trainingTranslationCurriculumService;
    @Spy private TrainingPageMapper trainingPageMapper = new TrainingPageMapperImpl();
    @Spy private FundingTypeTranslationMapper fundingTypeTranslationMapper = new FundingTypeTranslationMapperImpl();
    @Mock private LecturerService lecturerService;
    @Spy private PublicCourseSummaryMapper publicCourseSummaryMapper = new PublicCourseSummaryMapperImpl();
    @InjectMocks private TrainingPageService trainingPageService;
    private Training training;
    private Language estonian;
    private TrainingTranslation estonianTranslation;
    private TrainingTranslation englishTranslation;

    @BeforeEach
    void setUp() {
        estonian = language(1, "et");
        training = new Training();
        training.setId(10);
        training.setStatus("P");
        training.setIsOrderable(true);
        training.setIsPromoted(false);
        training.setTrainingLanguage(estonian);
        Category category = new Category();
        category.setId(3);
        training.setCategory(category);
        Location location = new Location();
        location.setName("BCS Koolitus");
        location.setIsOnline(false);
        training.setLocation(location);
        when(trainingService.getValidActiveTrainingBy(10)).thenReturn(training);
        estonianTranslation = translation(11, estonian, "Java algkursus");
        englishTranslation = translation(12, language(2, "en"), "Java for beginners");
    }

    @Test
    void requestedTranslationControlsTextPdfAndRelatedLanguageButNotTrainingLanguage() {
        when(trainingTranslationRepository.findTrainingTranslationBy(12, 10)).thenReturn(Optional.of(englishTranslation));
        TrainingTranslationCurriculumInfo trainingTranslationCurriculumInfo = mock(TrainingTranslationCurriculumInfo.class);
        when(trainingTranslationCurriculumInfo.getFileName()).thenReturn("Java_curriculum_en.pdf");
        when(trainingTranslationCurriculumInfo.getFileSize()).thenReturn(419430);
        when(trainingTranslationCurriculumService.findCurriculumInfo(12)).thenReturn(Optional.of(trainingTranslationCurriculumInfo));
        PublicCourseSummary publicCourseSummary = mock(PublicCourseSummary.class);
        when(publicCourseSummary.getCourseId()).thenReturn(22);
        when(publicCourseSummary.getStartDate()).thenReturn(LocalDate.of(2026, 10, 5));
        when(publicCourseSummary.getStatus()).thenReturn("F");
        when(publicCourseSummary.getIsOnline()).thenReturn(true);
        when(publicCourseSummaryRepository.findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(10, "en"))
                .thenReturn(List.of(publicCourseSummary));
        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "et", 12);
        assertEquals("Java for beginners", trainingPageDto.getTitle());
        assertEquals("et", trainingPageDto.getTrainingLanguageCode());
        assertFalse(trainingPageDto.getIsMainLanguageFallback());
        assertEquals("Java_curriculum_en.pdf", trainingPageDto.getCurriculumFileName());
        assertEquals(419430, trainingPageDto.getCurriculumFileSize());
        assertEquals(22, trainingPageDto.getUpcomingCourses().getFirst().getCourseId());
        assertEquals("F", trainingPageDto.getUpcomingCourses().getFirst().getStatus());
        assertTrue(trainingPageDto.getUpcomingCourses().getFirst().getIsOnline());
        verify(categoryTranslationRepository).findByCategory_IdAndLanguage_Id(3, 2);
        verify(fundingTypeTranslationRepository).findTrainingFundingTypeTranslationsBy(10, "en");
    }

    @Test
    void linkedLecturerSummariesUseUiLanguageAndPreserveLinkOrder() {
        when(trainingTranslationRepository.findTrainingTranslationBy(12, 10)).thenReturn(Optional.of(englishTranslation));
        Lecturer lecturer = new Lecturer();
        lecturer.setId(8);
        TrainingLecturer trainingLecturer = new TrainingLecturer();
        trainingLecturer.setLecturer(lecturer);
        when(trainingLecturerRepository.findTrainingLecturersBy(10)).thenReturn(List.of(trainingLecturer));
        LecturerSummaryDto lecturerSummaryDto = new LecturerSummaryDto(8, "Meelis Teern", "Lektor", "Java.", null);
        when(lecturerService.findLecturerSummariesBy(List.of(lecturer), "et")).thenReturn(List.of(lecturerSummaryDto));

        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "et", 12);

        assertEquals("Java for beginners", trainingPageDto.getTitle());
        assertEquals(List.of(lecturerSummaryDto), trainingPageDto.getLecturers());
        verify(lecturerService).findLecturerSummariesBy(List.of(lecturer), "et");
    }

    @Test
    void existingTranslationWithoutPdfDoesNotBorrowMainLanguagePdf() {
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(10, "en")).thenReturn(Optional.of(englishTranslation));
        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "en", null);
        assertNull(trainingPageDto.getCurriculumFileName());
        assertNull(trainingPageDto.getCurriculumFileSize());
        assertTrue(trainingPageDto.getFundingTypes().isEmpty());
        assertTrue(trainingPageDto.getLecturers().isEmpty());
        assertTrue(trainingPageDto.getUpcomingCourses().isEmpty());
        verify(trainingTranslationCurriculumService).findCurriculumInfo(12);
        verify(trainingTranslationCurriculumService, never()).findCurriculumInfo(11);
    }

    @Test
    void missingContentTranslationFallsBackTextButNeverPdf() {
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(10, "ru")).thenReturn(Optional.empty());
        when(languageService.getMainLanguage()).thenReturn(estonian);
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(10, "et")).thenReturn(Optional.of(estonianTranslation));
        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "ru", null);
        assertEquals("Java algkursus", trainingPageDto.getTitle());
        assertTrue(trainingPageDto.getIsMainLanguageFallback());
        assertNull(trainingPageDto.getCurriculumFileName());
        verifyNoInteractions(trainingTranslationCurriculumService);
    }

    @Test
    void foreignTranslationIdFallsBackToContentLanguage() {
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(10, "et")).thenReturn(Optional.of(estonianTranslation));
        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "et", 999);
        assertEquals(11, trainingPageDto.getTrainingTranslationId());
        assertFalse(trainingPageDto.getIsMainLanguageFallback());
        verify(trainingTranslationRepository).findTrainingTranslationBy(999, 10);
        verify(trainingTranslationCurriculumService).findCurriculumInfo(11);
        verify(trainingTranslationCurriculumService, never()).findCurriculumInfo(999);
    }

    @Test
    void draftPreviewRemainsAvailableAndDoesNotInventUpcomingCourses() {
        training.setStatus("U");
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(10, "et")).thenReturn(Optional.of(estonianTranslation));
        TrainingPageDto trainingPageDto = trainingPageService.getTrainingPage(10, "et", null);
        assertEquals("Java algkursus", trainingPageDto.getTitle());
        assertTrue(trainingPageDto.getUpcomingCourses().isEmpty());
    }

    @Test
    void deletedOrMissingTrainingStopsBeforeTranslationAndPdf() {
        when(trainingService.getValidActiveTrainingBy(10)).thenThrow(new PrimaryKeyNotFoundException("trainingId", 10));
        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingPageService.getTrainingPage(10, "et", 12));
        verifyNoInteractions(trainingTranslationRepository, trainingTranslationCurriculumService, publicCourseSummaryRepository);
    }

    @Test
    void noUsableTranslationIsNotFound() {
        when(languageService.getMainLanguage()).thenReturn(estonian);
        assertThrows(PrimaryKeyNotFoundException.class, () -> trainingPageService.getTrainingPage(10, "en", null));
        verifyNoInteractions(trainingTranslationCurriculumService, publicCourseSummaryRepository);
    }

    private Language language(int id, String code) {
        Language language = new Language();
        language.setId(id);
        language.setCode(code);
        language.setFlagIconCode(code.equals("et") ? "ee" : "gb");
        return language;
    }

    private TrainingTranslation translation(int id, Language language, String title) {
        TrainingTranslation trainingTranslation = new TrainingTranslation();
        trainingTranslation.setId(id);
        trainingTranslation.setTraining(training);
        trainingTranslation.setLanguage(language);
        trainingTranslation.setTitle(title);
        trainingTranslation.setShortDescription("Lühikirjeldus");
        trainingTranslation.setDescription("<p>Kirjeldus</p>");
        return trainingTranslation;
    }
}
