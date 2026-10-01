package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.course.dto.AdminCourseDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryItemDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummary;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceAdminCoursesTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseLecturerRepository courseLecturerRepository;
    @Mock
    private AdminCourseSummaryRepository adminCourseSummaryRepository;
    @Spy
    private AdminCourseSummaryMapper adminCourseSummaryMapper = new AdminCourseSummaryMapperImpl();

    @InjectMocks
    private CourseService courseService;

    // ---------- sorteerimine ----------

    @Test
    void createAdminCourseSort_missingOrUnknownSortByUsesDefaultOrder() {
        Sort defaultSort = Sort.by(Sort.Order.asc("isPast"), Sort.Order.asc("daysFromToday"), Sort.Order.asc("courseId"));
        assertEquals(defaultSort, CourseService.createAdminCourseSort(null, null));
        assertEquals(defaultSort, CourseService.createAdminCourseSort("notes", "ASC"));
    }

    @Test
    void createAdminCourseSort_mapsSortByToViewProperty() {
        assertEquals(Sort.by(Sort.Order.asc("startDate"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("startDate", "ASC"));
        assertEquals(Sort.by(Sort.Order.desc("trainingTitle"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("trainingTitle", "DESC"));
        assertEquals(Sort.by(Sort.Order.asc("price"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("price", "asc"));
        assertEquals(Sort.by(Sort.Order.asc("statusOrder"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("status", "ASC"));
        assertEquals(Sort.by(Sort.Order.desc("participantCount"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("participantCount", null));
        assertEquals(Sort.by(Sort.Order.asc("enquiryCount"), Sort.Order.asc("courseId")), CourseService.createAdminCourseSort("enquiryCount", "ASC"));
    }

    // ---------- nimekiri ----------

    @Test
    void findAdminCourses_usesPageLimitAndSort() {
        when(adminCourseSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        AdminCourseFilterDto adminCourseFilterDto = createAdminCourseFilterDto();
        adminCourseFilterDto.setSortBy("price");
        adminCourseFilterDto.setSortDirection("ASC");

        courseService.findAdminCourses(adminCourseFilterDto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminCourseSummaryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(PageRequest.of(1, 10, Sort.by(Sort.Order.asc("price"), Sort.Order.asc("courseId"))), pageableCaptor.getValue());
    }

    @Test
    void findAdminCourses_mapsRowsAndPageMetadata() {
        Page<AdminCourseSummary> adminCourseSummaryPage = new PageImpl<>(List.of(createAdminCourseSummary()), PageRequest.of(0, 10), 12);
        when(adminCourseSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adminCourseSummaryPage);

        AdminCourseSummaryDto adminCourseSummaryDto = courseService.findAdminCourses(createAdminCourseFilterDto());

        assertEquals(2, adminCourseSummaryDto.getTotalPages());
        assertEquals(12L, adminCourseSummaryDto.getTotalElements());
        AdminCourseSummaryItemDto adminCourseSummaryItemDto = adminCourseSummaryDto.getAdminCourseSummaries().getFirst();
        assertEquals(1, adminCourseSummaryItemDto.getCourseId());
        assertEquals(1, adminCourseSummaryItemDto.getTrainingId());
        assertEquals("Java algkursus", adminCourseSummaryItemDto.getTrainingTitle());
        assertEquals(LocalDate.of(2026, 10, 5), adminCourseSummaryItemDto.getStartDate());
        assertEquals(false, adminCourseSummaryItemDto.getIsPast());
        assertEquals(true, adminCourseSummaryItemDto.getIsPromoted());
        assertEquals(false, adminCourseSummaryItemDto.getHasMeetingLink());
        assertEquals(3L, adminCourseSummaryItemDto.getParticipantCount());
        assertEquals(2L, adminCourseSummaryItemDto.getPaidCount());
        assertEquals(1L, adminCourseSummaryItemDto.getEnquiryCount());
    }

    @Test
    void findAdminCourses_noResults_returnsEmptyList() {
        when(adminCourseSummaryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        AdminCourseSummaryDto adminCourseSummaryDto = courseService.findAdminCourses(createAdminCourseFilterDto());

        assertEquals(0L, adminCourseSummaryDto.getTotalElements());
        assertTrue(adminCourseSummaryDto.getAdminCourseSummaries().isEmpty());
    }

    // ---------- üks toimumiskord ----------

    @Test
    void getAdminCourse_combinesViewRowAndCourseData() {
        when(courseRepository.findById(1)).thenReturn(Optional.of(createCourse("O")));
        when(adminCourseSummaryRepository.findByCourseIdAndContentLanguageCode(1, "et")).thenReturn(Optional.of(createAdminCourseSummary()));
        when(courseLecturerRepository.findCourseLecturersBy(1)).thenReturn(List.of(createCourseLecturer("Rain Tüür"), createCourseLecturer("Meelis Teern")));

        AdminCourseDto adminCourseDto = courseService.getAdminCourse(1, "et");

        assertEquals(1, adminCourseDto.getCourseId());
        assertEquals(1, adminCourseDto.getTrainingTranslationId());
        assertEquals("Java algkursus", adminCourseDto.getTrainingTitle());
        assertEquals(40, adminCourseDto.getNumberOfAcademicHours());
        assertEquals("Rain Tüür, Meelis Teern", adminCourseDto.getLecturerNames());
        assertEquals("Assauwe", adminCourseDto.getRoomName());
        assertNull(adminCourseDto.getMeetingLink());
        assertEquals("Kaasa sülearvuti.", adminCourseDto.getNotes());
    }

    @Test
    void getAdminCourse_noLecturersOrRoom_returnsNulls() {
        Course course = createCourse("O");
        course.setRoom(null);
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(adminCourseSummaryRepository.findByCourseIdAndContentLanguageCode(1, "et")).thenReturn(Optional.of(createAdminCourseSummary()));
        when(courseLecturerRepository.findCourseLecturersBy(1)).thenReturn(List.of());

        AdminCourseDto adminCourseDto = courseService.getAdminCourse(1, "et");

        assertNull(adminCourseDto.getLecturerNames());
        assertNull(adminCourseDto.getRoomName());
    }

    @Test
    void getAdminCourse_deletedCourseThrows() {
        when(courseRepository.findById(8)).thenReturn(Optional.of(createCourse("D")));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.getAdminCourse(8, "et"));

        assertEquals("Ei leidnud primary keyd 'courseId' väärtusega: 8", exception.getMessage());
    }

    @Test
    void getAdminCourse_deletedTrainingThrows() {
        AdminCourseSummary adminCourseSummary = createAdminCourseSummary();
        ReflectionTestUtils.setField(adminCourseSummary, "trainingStatus", "D");
        when(courseRepository.findById(1)).thenReturn(Optional.of(createCourse("O")));
        when(adminCourseSummaryRepository.findByCourseIdAndContentLanguageCode(1, "et")).thenReturn(Optional.of(adminCourseSummary));

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.getAdminCourse(1, "et"));
    }

    private static AdminCourseFilterDto createAdminCourseFilterDto() {
        AdminCourseFilterDto adminCourseFilterDto = new AdminCourseFilterDto();
        adminCourseFilterDto.setContentLang("et");
        adminCourseFilterDto.setPage(1);
        adminCourseFilterDto.setLimit(10);
        return adminCourseFilterDto;
    }

    // 3_import.sql toimumiskord 1
    private static AdminCourseSummary createAdminCourseSummary() {
        AdminCourseSummary adminCourseSummary = new AdminCourseSummary();
        ReflectionTestUtils.setField(adminCourseSummary, "courseId", 1);
        ReflectionTestUtils.setField(adminCourseSummary, "contentLanguageCode", "et");
        ReflectionTestUtils.setField(adminCourseSummary, "trainingId", 1);
        ReflectionTestUtils.setField(adminCourseSummary, "trainingTranslationId", 1);
        ReflectionTestUtils.setField(adminCourseSummary, "trainingTitle", "Java algkursus");
        ReflectionTestUtils.setField(adminCourseSummary, "trainingStatus", "P");
        ReflectionTestUtils.setField(adminCourseSummary, "startDate", LocalDate.of(2026, 10, 5));
        ReflectionTestUtils.setField(adminCourseSummary, "endDate", LocalDate.of(2026, 10, 9));
        ReflectionTestUtils.setField(adminCourseSummary, "isPast", false);
        ReflectionTestUtils.setField(adminCourseSummary, "numberOfDays", 5);
        ReflectionTestUtils.setField(adminCourseSummary, "price", new BigDecimal("490.0000"));
        ReflectionTestUtils.setField(adminCourseSummary, "status", "O");
        ReflectionTestUtils.setField(adminCourseSummary, "isPromoted", true);
        ReflectionTestUtils.setField(adminCourseSummary, "isOnSite", true);
        ReflectionTestUtils.setField(adminCourseSummary, "hasMeetingLink", false);
        ReflectionTestUtils.setField(adminCourseSummary, "participantCount", 3L);
        ReflectionTestUtils.setField(adminCourseSummary, "paidCount", 2L);
        ReflectionTestUtils.setField(adminCourseSummary, "enquiryCount", 1L);
        return adminCourseSummary;
    }

    private static Course createCourse(String status) {
        Room room = new Room();
        room.setId(1);
        room.setName("Assauwe");
        Course course = new Course();
        course.setId(1);
        course.setStatus(status);
        course.setNumberOfAcademicHours(40);
        course.setRoom(room);
        course.setNotes("Kaasa sülearvuti.");
        return course;
    }

    private static CourseLecturer createCourseLecturer(String fullName) {
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(fullName);
        CourseLecturer courseLecturer = new CourseLecturer();
        courseLecturer.setLecturer(lecturer);
        return courseLecturer;
    }
}
