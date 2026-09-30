package ee.bcskoolitus.service;

import ee.bcskoolitus.LecturerStatus;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateResponseDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerDetailDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerProfileDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerSummaryDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTrainingDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationUpdateDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.LecturerMapper;
import ee.bcskoolitus.persistance.lecturer.LecturerMapperImpl;
import ee.bcskoolitus.persistance.lecturer.LecturerRepository;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslation;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapper;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapperImpl;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Kustutatud koolitaja (status D) on getValidActiveLecturerBy ja avalike teenuste jaoks nagu olematu;
// getValidLecturerBy leiab ta üles (delete, restore)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LecturerServiceTest {

    private static final Integer LECTURER_ID = 4;

    @Mock
    private LecturerRepository lecturerRepository;
    @Mock
    private LecturerTranslationRepository lecturerTranslationRepository;
    @Mock
    private LecturerTranslationService lecturerTranslationService;
    @Mock
    private LecturerPhotoService lecturerPhotoService;
    @Mock
    private CourseLecturerRepository courseLecturerRepository;
    @Mock
    private TrainingLecturerRepository trainingLecturerRepository;
    @Mock
    private AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    @Mock
    private UserService userService;
    @Mock
    private LanguageService languageService;
    @Spy
    private LecturerMapper lecturerMapper = new LecturerMapperImpl();
    @Spy
    private LecturerTranslationMapper lecturerTranslationMapper = new LecturerTranslationMapperImpl();

    @InjectMocks
    private LecturerService lecturerService;

    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        lecturer = new Lecturer();
        lecturer.setId(LECTURER_ID);
        lecturer.setFullName("Kersti Laidvee");
        lecturer.setStatus(LecturerStatus.ACTIVE.getCode());
        when(lecturerRepository.findById(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(languageService.getMainLanguage()).thenReturn(createLanguage(1, "et"));
    }

    @Test
    void findLecturers_searchesOnlyActiveLecturersWithTrimmedSearch() {
        when(lecturerRepository.findLecturersBy("kersti", LecturerStatus.ACTIVE.getCode())).thenReturn(List.of(lecturer));

        List<LecturerDto> lecturerDtos = lecturerService.findLecturers("  kersti ");

        assertEquals(List.of(new LecturerDto(LECTURER_ID, "Kersti Laidvee")), lecturerDtos);
    }

    @Test
    void getValidActiveLecturerBy_deletedLecturer_throwsPrimaryKeyNotFound() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerService.getValidActiveLecturerBy(LECTURER_ID, "lecturerId"));

        assertEquals("Ei leidnud primary keyd 'lecturerId' väärtusega: 4", exception.getMessage());
    }

    @Test
    void getValidLecturerBy_deletedLecturer_returnsLecturer() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertSame(lecturer, lecturerService.getValidLecturerBy(LECTURER_ID, "lecturerId"));
    }

    @Test
    void getValidAssignableLecturerBy_linkedDeletedLecturerIsAllowed() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertSame(lecturer, lecturerService.getValidAssignableLecturerBy(LECTURER_ID, List.of(LECTURER_ID)));
    }

    @Test
    void getValidAssignableLecturerBy_newDeletedLecturerThrows() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerService.getValidAssignableLecturerBy(LECTURER_ID, List.of(1)));

        assertEquals("Ei leidnud primary keyd 'lecturerId' väärtusega: 4", exception.getMessage());
    }

    @Test
    void getLecturer_returnsNameAndPhotoVersion() {
        when(lecturerPhotoService.findPhotoVersion(LECTURER_ID)).thenReturn(1784095200L);

        assertEquals(new LecturerDetailDto(LECTURER_ID, "Kersti Laidvee", 1784095200L), lecturerService.getLecturer(LECTURER_ID));
    }

    @Test
    void getLecturer_deletedLecturer_throwsPrimaryKeyNotFound() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertThrows(PrimaryKeyNotFoundException.class, () -> lecturerService.getLecturer(LECTURER_ID));
    }

    @Test
    void addLecturer_createsActiveLecturerWithMainLanguageTranslationAndPhoto() {
        User user = new User();
        user.setId(1);
        when(userService.getValidUserBy(1)).thenReturn(user);
        LecturerCreateRequestDto lecturerCreateRequestDto = new LecturerCreateRequestDto(1, "Kristjan Kuusk", "cGhvdG8=", "image/png",
                "Andmeinsener", "SQL ja andmeanalüüs.", "<p onclick=\"x\">Kristjan</p><script>x</script>");

        lecturerService.addLecturer(lecturerCreateRequestDto);

        ArgumentCaptor<Lecturer> lecturerCaptor = ArgumentCaptor.forClass(Lecturer.class);
        verify(lecturerRepository).save(lecturerCaptor.capture());
        Lecturer savedLecturer = lecturerCaptor.getValue();
        assertEquals("Kristjan Kuusk", savedLecturer.getFullName());
        assertEquals(LecturerStatus.ACTIVE.getCode(), savedLecturer.getStatus());
        assertSame(user, savedLecturer.getCreatedBy());
        verify(lecturerPhotoService).saveLecturerPhoto(savedLecturer, "cGhvdG8=", "image/png");
        ArgumentCaptor<LecturerTranslation> lecturerTranslationCaptor = ArgumentCaptor.forClass(LecturerTranslation.class);
        verify(lecturerTranslationRepository).save(lecturerTranslationCaptor.capture());
        LecturerTranslation savedLecturerTranslation = lecturerTranslationCaptor.getValue();
        assertEquals("Andmeinsener", savedLecturerTranslation.getTitle());
        assertEquals("<p>Kristjan</p>", savedLecturerTranslation.getDescription());
        assertEquals("et", savedLecturerTranslation.getLanguage().getCode());
        assertSame(savedLecturer, savedLecturerTranslation.getLecturer());
    }

    @Test
    void addLecturer_withoutPhotoDoesNotSavePhoto() {
        when(userService.getValidUserBy(1)).thenReturn(new User());
        LecturerCreateRequestDto lecturerCreateRequestDto = new LecturerCreateRequestDto(1, "Kristjan Kuusk", null, null,
                "Andmeinsener", "SQL.", "<p>SQL</p>");

        LecturerCreateResponseDto lecturerCreateResponseDto = lecturerService.addLecturer(lecturerCreateRequestDto);

        verifyNoInteractions(lecturerPhotoService);
        assertNull(lecturerCreateResponseDto.getLecturerId());
    }

    @Test
    void updateLecturer_updatesNameAndTranslationAndKeepsPhotoWhenPhotoIsNull() {
        LecturerTranslation lecturerTranslation = new LecturerTranslation();
        lecturerTranslation.setId(5);
        when(lecturerTranslationService.getValidLecturerTranslationBy(5, LECTURER_ID)).thenReturn(lecturerTranslation);

        lecturerService.updateLecturer(LECTURER_ID, createLecturerUpdateRequestDto(null, false));

        assertEquals("Kersti Laidvee-Kask", lecturer.getFullName());
        assertEquals("Lektor", lecturerTranslation.getTitle());
        assertEquals("Figma.", lecturerTranslation.getShortDescription());
        assertEquals("<p>Figma</p>", lecturerTranslation.getDescription());
        assertEquals(5, lecturerTranslation.getId());
        verify(lecturerRepository).save(lecturer);
        verify(lecturerTranslationRepository).save(lecturerTranslation);
        verifyNoInteractions(lecturerPhotoService);
    }

    @Test
    void updateLecturer_newPhotoIsSaved() {
        when(lecturerTranslationService.getValidLecturerTranslationBy(5, LECTURER_ID)).thenReturn(new LecturerTranslation());

        lecturerService.updateLecturer(LECTURER_ID, createLecturerUpdateRequestDto("cGhvdG8=", false));

        verify(lecturerPhotoService).saveLecturerPhoto(lecturer, "cGhvdG8=", "image/png");
        verify(lecturerPhotoService, never()).deleteLecturerPhoto(anyInt());
    }

    @Test
    void updateLecturer_isPhotoRemovedDeletesPhoto() {
        when(lecturerTranslationService.getValidLecturerTranslationBy(5, LECTURER_ID)).thenReturn(new LecturerTranslation());

        lecturerService.updateLecturer(LECTURER_ID, createLecturerUpdateRequestDto(null, true));

        verify(lecturerPhotoService).deleteLecturerPhoto(LECTURER_ID);
        verify(lecturerPhotoService, never()).saveLecturerPhoto(any(), any(), any());
    }

    @Test
    void updateLecturer_translationOfAnotherLecturerThrowsAndSavesNothing() {
        when(lecturerTranslationService.getValidLecturerTranslationBy(5, LECTURER_ID))
                .thenThrow(new PrimaryKeyNotFoundException("lecturerTranslationId", 5));

        assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerService.updateLecturer(LECTURER_ID, createLecturerUpdateRequestDto(null, false)));

        verify(lecturerRepository, never()).save(any());
        verifyNoInteractions(lecturerPhotoService);
    }

    @Test
    void updateLecturer_deletedLecturerThrows() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerService.updateLecturer(LECTURER_ID, createLecturerUpdateRequestDto(null, false)));

        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void addLecturerTranslation_savesSanitizedTranslation() {
        Language language = createLanguage(2, "en");
        when(languageService.getValidLanguageBy(2, "languageId")).thenReturn(language);

        lecturerService.addLecturerTranslation(LECTURER_ID, new LecturerTranslationCreateRequestDto(2, "Lecturer", "Figma.", "<p>Figma<script>x</script></p>"));

        ArgumentCaptor<LecturerTranslation> lecturerTranslationCaptor = ArgumentCaptor.forClass(LecturerTranslation.class);
        verify(lecturerTranslationRepository).save(lecturerTranslationCaptor.capture());
        assertEquals("<p>Figma</p>", lecturerTranslationCaptor.getValue().getDescription());
        assertSame(language, lecturerTranslationCaptor.getValue().getLanguage());
        assertSame(lecturer, lecturerTranslationCaptor.getValue().getLecturer());
    }

    @Test
    void addLecturerTranslation_existingLanguageThrowsTranslationExists() {
        when(languageService.getValidLanguageBy(2, "languageId")).thenReturn(createLanguage(2, "en"));
        when(lecturerTranslationRepository.existsByLecturer_IdAndLanguage_Id(LECTURER_ID, 2)).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> lecturerService.addLecturerTranslation(LECTURER_ID, new LecturerTranslationCreateRequestDto(2, "Lecturer", "Figma.", "<p>Figma</p>")));

        assertEquals("TRANSLATION_EXISTS", exception.getErrorCode());
        verify(lecturerTranslationRepository, never()).save(any());
    }

    @Test
    void deleteLecturer_withoutUpcomingCoursesSetsDeletedStatus() {
        when(courseLecturerRepository.countUpcomingCourseLecturersBy(eq(LECTURER_ID), any(), eq(List.of("X", "D")))).thenReturn(0L);

        lecturerService.deleteLecturer(LECTURER_ID);

        assertEquals(LecturerStatus.DELETED.getCode(), lecturer.getStatus());
        verify(lecturerRepository).save(lecturer);
    }

    @Test
    void deleteLecturer_withUpcomingCoursesThrows() {
        when(courseLecturerRepository.countUpcomingCourseLecturersBy(eq(LECTURER_ID), any(), any())).thenReturn(1L);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> lecturerService.deleteLecturer(LECTURER_ID));

        assertEquals("LECTURER_HAS_UPCOMING_COURSES", exception.getErrorCode());
        assertEquals(LecturerStatus.ACTIVE.getCode(), lecturer.getStatus());
        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void deleteLecturer_alreadyDeletedChangesNothing() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        lecturerService.deleteLecturer(LECTURER_ID);

        verify(lecturerRepository, never()).save(any());
        verifyNoInteractions(courseLecturerRepository);
    }

    @Test
    void deleteLecturer_unknownLecturerThrows() {
        when(lecturerRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> lecturerService.deleteLecturer(123));
    }

    @Test
    void restoreLecturer_deletedLecturerBecomesActive() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        lecturerService.restoreLecturer(LECTURER_ID);

        assertEquals(LecturerStatus.ACTIVE.getCode(), lecturer.getStatus());
        verify(lecturerRepository).save(lecturer);
    }

    @Test
    void restoreLecturer_activeLecturerChangesNothing() {
        lecturerService.restoreLecturer(LECTURER_ID);

        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void getLecturerSummary_usesFirstDisplayTranslationAndPhotoVersion() {
        LecturerTranslation englishTranslation = createLecturerTranslation("Lecturer", "Figma.");
        LecturerTranslation estonianTranslation = createLecturerTranslation("Lektor", "Figma et.");
        when(lecturerTranslationRepository.findDisplayLecturerTranslationsBy(List.of(LECTURER_ID), "en"))
                .thenReturn(List.of(englishTranslation, estonianTranslation));
        when(lecturerPhotoService.findPhotoVersions(List.of(LECTURER_ID))).thenReturn(Map.of(LECTURER_ID, 100L));

        LecturerSummaryDto lecturerSummaryDto = lecturerService.getLecturerSummary(LECTURER_ID, "en");

        assertEquals(new LecturerSummaryDto(LECTURER_ID, "Kersti Laidvee", "Lecturer", "Figma.", 100L), lecturerSummaryDto);
    }

    @Test
    void getLecturerSummary_deletedLecturerThrows() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertThrows(PrimaryKeyNotFoundException.class, () -> lecturerService.getLecturerSummary(LECTURER_ID, "et"));
    }

    @Test
    void findLecturerSummaries_returnsActiveLecturersWithoutPhotoVersionWhenNoPhoto() {
        when(lecturerRepository.findAllByStatusOrderByFullNameAscIdAsc(LecturerStatus.ACTIVE.getCode())).thenReturn(List.of(lecturer));
        when(lecturerTranslationRepository.findDisplayLecturerTranslationsBy(List.of(LECTURER_ID), "et"))
                .thenReturn(List.of(createLecturerTranslation("Lektor", "Figma.")));
        when(lecturerPhotoService.findPhotoVersions(List.of(LECTURER_ID))).thenReturn(Map.of());

        List<LecturerSummaryDto> lecturerSummaryDtos = lecturerService.findLecturerSummaries("et");

        assertEquals(List.of(new LecturerSummaryDto(LECTURER_ID, "Kersti Laidvee", "Lektor", "Figma.", null)), lecturerSummaryDtos);
    }

    @Test
    void getLecturerProfile_returnsSanitizedDescriptionAndPublishedTrainings() {
        LecturerTranslation lecturerTranslation = createLecturerTranslation("Lektor", "Figma.");
        lecturerTranslation.setDescription("<p>Figma<script>x</script></p>");
        when(lecturerTranslationRepository.findDisplayLecturerTranslationsBy(List.of(LECTURER_ID), "et")).thenReturn(List.of(lecturerTranslation));
        when(trainingLecturerRepository.findTrainingIdsBy(LECTURER_ID)).thenReturn(List.of(6));
        when(adminTrainingSummaryRepository.findAllByContentLanguageCodeAndStatusAndTraining_IdInOrderByTitleAsc("et", "P", List.of(6)))
                .thenReturn(List.of(createAdminTrainingSummary(6, 11, "Figma praktikum")));

        LecturerProfileDto lecturerProfileDto = lecturerService.getLecturerProfile(LECTURER_ID, "et");

        assertEquals("<p>Figma</p>", lecturerProfileDto.getDescription());
        assertEquals(List.of(new LecturerTrainingDto(6, 11, "Figma praktikum")), lecturerProfileDto.getTrainings());
    }

    @Test
    void getLecturerProfile_withoutTrainingsReturnsEmptyList() {
        when(lecturerTranslationRepository.findDisplayLecturerTranslationsBy(any(), any())).thenReturn(List.of());
        when(trainingLecturerRepository.findTrainingIdsBy(LECTURER_ID)).thenReturn(List.of());

        LecturerProfileDto lecturerProfileDto = lecturerService.getLecturerProfile(LECTURER_ID, "et");

        assertEquals(List.of(), lecturerProfileDto.getTrainings());
        verifyNoInteractions(adminTrainingSummaryRepository);
    }

    private LecturerUpdateRequestDto createLecturerUpdateRequestDto(String photo, boolean isPhotoRemoved) {
        return new LecturerUpdateRequestDto("Kersti Laidvee-Kask", photo, photo == null ? null : "image/png", isPhotoRemoved,
                new LecturerTranslationUpdateDto(5, "Lektor", "Figma.", "<p>Figma</p><script>x</script>"));
    }

    private LecturerTranslation createLecturerTranslation(String title, String shortDescription) {
        LecturerTranslation lecturerTranslation = new LecturerTranslation();
        lecturerTranslation.setLecturer(lecturer);
        lecturerTranslation.setTitle(title);
        lecturerTranslation.setShortDescription(shortDescription);
        return lecturerTranslation;
    }

    private static AdminTrainingSummary createAdminTrainingSummary(Integer trainingId, Integer trainingTranslationId, String title) {
        Training training = new Training();
        training.setId(trainingId);
        AdminTrainingSummary adminTrainingSummary = new AdminTrainingSummary();
        ReflectionTestUtils.setField(adminTrainingSummary, "training", training);
        ReflectionTestUtils.setField(adminTrainingSummary, "trainingTranslationId", trainingTranslationId);
        ReflectionTestUtils.setField(adminTrainingSummary, "title", title);
        return adminTrainingSummary;
    }

    private static Language createLanguage(Integer languageId, String code) {
        Language language = new Language();
        language.setId(languageId);
        language.setCode(code);
        return language;
    }
}
