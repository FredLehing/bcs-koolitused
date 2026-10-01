package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.course.dto.CoursePageDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryPageDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseSummaryItemDto;
import ee.bcskoolitus.controller.course.dto.UpcomingCourseDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.Category;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslation;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationRepository;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.CourseMapper;
import ee.bcskoolitus.persistance.course.CourseMapperImpl;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapper;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapperImpl;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslation;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapperImpl;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummary;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseServicePublicCoursesTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseLecturerRepository courseLecturerRepository;
    @Mock
    private PublicCourseSummaryRepository publicCourseSummaryRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;
    @Mock
    private CategoryTranslationRepository categoryTranslationRepository;
    @Mock
    private FundingTypeTranslationRepository fundingTypeTranslationRepository;
    @Mock
    private LanguageService languageService;
    @Spy
    private CourseMapper courseMapper = new CourseMapperImpl();
    @Spy
    private CourseLecturerMapper courseLecturerMapper = new CourseLecturerMapperImpl();
    @Spy
    private PublicCourseSummaryMapper publicCourseSummaryMapper = new PublicCourseSummaryMapperImpl();
    @Spy
    private FundingTypeTranslationMapper fundingTypeTranslationMapper = new FundingTypeTranslationMapperImpl();

    @InjectMocks
    private CourseService courseService;

    private static final Language ESTONIAN = createLanguage(1, "et");

    // ---------- avalik kalender ----------

    @Test
    void findPublicCourses_promotedFirstThenByStartDate() {
        when(publicCourseSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        courseService.findPublicCourses(createPublicCourseFilterDto());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicCourseSummaryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(PageRequest.of(0, 6, Sort.by(Sort.Order.desc("isPromoted"), Sort.Order.asc("startDate"), Sort.Order.asc("courseId"))),
                pageableCaptor.getValue());
    }

    @Test
    void findPublicCourses_mapsRowsAndAddsFundingTypes() {
        when(publicCourseSummaryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(createPublicCourseSummary()), PageRequest.of(0, 6), 7));
        when(fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(3, "et"))
                .thenReturn(List.of(createFundingTypeTranslation(1, "Töötukassa")));

        CourseSummaryPageDto courseSummaryPageDto = courseService.findPublicCourses(createPublicCourseFilterDto());

        assertEquals(2, courseSummaryPageDto.getTotalPages());
        assertEquals(7L, courseSummaryPageDto.getTotalElements());
        PublicCourseSummaryItemDto publicCourseSummaryItemDto = courseSummaryPageDto.getCourseSummaries().getFirst();
        assertEquals(9, publicCourseSummaryItemDto.getCourseId());
        assertEquals("Spring Boot veebiarendus", publicCourseSummaryItemDto.getTitle());
        assertEquals("Rain Tüür", publicCourseSummaryItemDto.getLecturerNames());
        assertEquals(true, publicCourseSummaryItemDto.getIsPromoted());
        assertEquals(true, publicCourseSummaryItemDto.getIsOnSite());
        assertEquals(false, publicCourseSummaryItemDto.getIsOnline());
        assertEquals(List.of(new FundingTypeDto(1, "Töötukassa")), publicCourseSummaryItemDto.getFundingTypes());
    }

    // ---------- toimumiskorra leht ----------

    @Test
    void getCoursePage_returnsTranslationInContentLang() {
        Course course = createCourse("O", "P");
        when(courseRepository.findById(9)).thenReturn(Optional.of(course));
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(3, "et")).thenReturn(Optional.of(createTrainingTranslation(5, ESTONIAN)));
        when(categoryTranslationRepository.findByCategory_IdAndLanguage_Id(1, 1)).thenReturn(Optional.of(createCategoryTranslation("Programmeerimine")));
        when(courseLecturerRepository.findCourseLecturersBy(9)).thenReturn(List.of(createCourseLecturer(1, "Rain Tüür")));
        when(publicCourseSummaryRepository.findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(3, "et"))
                .thenReturn(List.of(createPublicCourseSummary()));

        CoursePageDto coursePageDto = courseService.getCoursePage(9, "et");

        assertEquals(9, coursePageDto.getCourseId());
        assertEquals(3, coursePageDto.getTrainingId());
        assertEquals(5, coursePageDto.getTrainingTranslationId());
        assertFalse(coursePageDto.getIsMainLanguageFallback());
        assertEquals("Spring Boot veebiarendus", coursePageDto.getTitle());
        assertEquals("<p>Kirjeldus</p>", coursePageDto.getDescription());
        assertEquals("Programmeerimine", coursePageDto.getCategoryName());
        assertEquals("fi-ee", coursePageDto.getTrainingLanguageFlagIconCode());
        assertEquals(32, coursePageDto.getNumberOfAcademicHours());
        assertTrue(coursePageDto.getIsOnSite());
        assertFalse(coursePageDto.getIsOnline());
        assertFalse(coursePageDto.getIsPast());
        assertEquals(List.of(new LecturerDto(1, "Rain Tüür")), coursePageDto.getLecturers());
        assertEquals(List.of(new UpcomingCourseDto(9, LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 15), "O", true, false)),
                coursePageDto.getUpcomingCourses());
    }

    @Test
    void getCoursePage_missingTranslationFallsBackToMainLanguage() {
        when(courseRepository.findById(9)).thenReturn(Optional.of(createCourse("F", "P")));
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(3, "en")).thenReturn(Optional.empty());
        when(languageService.getMainLanguage()).thenReturn(ESTONIAN);
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(3, "et")).thenReturn(Optional.of(createTrainingTranslation(5, ESTONIAN)));

        CoursePageDto coursePageDto = courseService.getCoursePage(9, "en");

        assertTrue(coursePageDto.getIsMainLanguageFallback());
        assertEquals("Spring Boot veebiarendus", coursePageDto.getTitle());
        assertNull(coursePageDto.getCategoryName());
        verify(publicCourseSummaryRepository).findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(3, "et");
    }

    @Test
    void getCoursePage_pastPublicCourseIsFound() {
        Course course = createCourse("O", "P");
        course.setStartDate(LocalDate.of(2026, 6, 8));
        course.setEndDate(LocalDate.of(2026, 6, 12));
        when(courseRepository.findById(9)).thenReturn(Optional.of(course));
        when(trainingTranslationRepository.findByTraining_IdAndLanguage_Code(3, "et")).thenReturn(Optional.of(createTrainingTranslation(5, ESTONIAN)));

        assertTrue(courseService.getCoursePage(9, "et").getIsPast());
    }

    @Test
    void getCoursePage_nonPublicCourseThrows() {
        for (String courseStatus : List.of("U", "X", "D")) {
            when(courseRepository.findById(9)).thenReturn(Optional.of(createCourse(courseStatus, "P")));
            PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.getCoursePage(9, "et"));
            assertEquals("Ei leidnud primary keyd 'courseId' väärtusega: 9", exception.getMessage());
        }
        when(courseRepository.findById(9)).thenReturn(Optional.of(createCourse("O", "U")));
        assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.getCoursePage(9, "et"));
    }

    private static PublicCourseFilterDto createPublicCourseFilterDto() {
        PublicCourseFilterDto publicCourseFilterDto = new PublicCourseFilterDto();
        publicCourseFilterDto.setContentLang("et");
        publicCourseFilterDto.setPage(0);
        publicCourseFilterDto.setLimit(6);
        return publicCourseFilterDto;
    }

    // 3_import.sql toimumiskord 9 (koolitus 3, Eppingi, esile tõstetud)
    private static PublicCourseSummary createPublicCourseSummary() {
        PublicCourseSummary publicCourseSummary = new PublicCourseSummary();
        ReflectionTestUtils.setField(publicCourseSummary, "courseId", 9);
        ReflectionTestUtils.setField(publicCourseSummary, "trainingId", 3);
        ReflectionTestUtils.setField(publicCourseSummary, "title", "Spring Boot veebiarendus");
        ReflectionTestUtils.setField(publicCourseSummary, "startDate", LocalDate.of(2026, 10, 12));
        ReflectionTestUtils.setField(publicCourseSummary, "endDate", LocalDate.of(2026, 10, 15));
        ReflectionTestUtils.setField(publicCourseSummary, "status", "O");
        ReflectionTestUtils.setField(publicCourseSummary, "isPromoted", true);
        ReflectionTestUtils.setField(publicCourseSummary, "isOnSite", true);
        ReflectionTestUtils.setField(publicCourseSummary, "isOnline", false);
        ReflectionTestUtils.setField(publicCourseSummary, "lecturerNames", "Rain Tüür");
        return publicCourseSummary;
    }

    private static Course createCourse(String courseStatus, String trainingStatus) {
        Category category = new Category();
        category.setId(1);
        Training training = new Training();
        training.setId(3);
        training.setStatus(trainingStatus);
        training.setCategory(category);
        Language trainingLanguage = createLanguage(1, "et");
        trainingLanguage.setFlagIconCode("fi-ee");
        training.setTrainingLanguage(trainingLanguage);
        Course course = new Course();
        course.setId(9);
        course.setTraining(training);
        course.setStatus(courseStatus);
        course.setStartDate(LocalDate.now().plusDays(11));
        course.setEndDate(LocalDate.now().plusDays(14));
        course.setNumberOfDays(4);
        course.setNumberOfAcademicHours(32);
        course.setPrice(new BigDecimal("560.0000"));
        course.setRoom(new Room());
        course.setMeetingLink(" ");
        return course;
    }

    private static TrainingTranslation createTrainingTranslation(Integer trainingTranslationId, Language language) {
        TrainingTranslation trainingTranslation = new TrainingTranslation();
        trainingTranslation.setId(trainingTranslationId);
        trainingTranslation.setLanguage(language);
        trainingTranslation.setTitle("Spring Boot veebiarendus");
        trainingTranslation.setShortDescription("REST API-de loomine Spring Booti abil.");
        trainingTranslation.setDescription("<p>Kirjeldus</p>");
        return trainingTranslation;
    }

    private static CategoryTranslation createCategoryTranslation(String name) {
        CategoryTranslation categoryTranslation = new CategoryTranslation();
        categoryTranslation.setName(name);
        return categoryTranslation;
    }

    private static CourseLecturer createCourseLecturer(Integer lecturerId, String fullName) {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFullName(fullName);
        CourseLecturer courseLecturer = new CourseLecturer();
        courseLecturer.setLecturer(lecturer);
        return courseLecturer;
    }

    private static FundingTypeTranslation createFundingTypeTranslation(Integer fundingTypeId, String name) {
        FundingType fundingType = new FundingType();
        fundingType.setId(fundingTypeId);
        FundingTypeTranslation fundingTypeTranslation = new FundingTypeTranslation();
        fundingTypeTranslation.setFundingType(fundingType);
        fundingTypeTranslation.setName(name);
        return fundingTypeTranslation;
    }

    private static Language createLanguage(Integer languageId, String code) {
        Language language = new Language();
        language.setId(languageId);
        language.setCode(code);
        return language;
    }
}
