package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CourseUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.CourseMapper;
import ee.bcskoolitus.persistance.course.CourseMapperImpl;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapper;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapperImpl;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryMapper;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseServiceTest {

    private static final Integer COURSE_ID = 4;
    private static final Integer TRAINING_ID = 1;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseLecturerRepository courseLecturerRepository;
    @Mock
    private CourseSummaryRepository courseSummaryRepository;
    @Mock
    private TrainingService trainingService;
    @Mock
    private LecturerService lecturerService;
    @Mock
    private RoomService roomService;
    @Mock
    private UserService userService;
    @Spy
    private CourseMapper courseMapper = new CourseMapperImpl();
    @Spy
    private CourseLecturerMapper courseLecturerMapper = new CourseLecturerMapperImpl();
    @Spy
    private CourseSummaryMapper courseSummaryMapper = new CourseSummaryMapperImpl();

    @InjectMocks
    private CourseService courseService;

    private Training training;
    private Course course;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(TRAINING_ID);
        course = new Course();
        course.setId(COURSE_ID);
        course.setTraining(training);
        course.setStatus(CourseStatus.OPEN.getCode());
        User user = new User();
        user.setId(9);
        course.setCreatedBy(user);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(trainingService.getValidActiveTrainingBy(TRAINING_ID)).thenReturn(training);
        when(roomService.getValidRoomBy(2)).thenReturn(createRoom(2));
        when(userService.getValidUserBy(1)).thenReturn(new User());
        when(lecturerService.getValidAssignableLecturerBy(any(), any()))
                .thenAnswer(invocation -> createLecturer(invocation.getArgument(0), "Koolitaja " + invocation.getArgument(0)));
    }

    @Test
    void findTrainingCourses_defaultReturnsOnlyUpcomingWithoutDeleted() {
        courseService.findTrainingCourses(TRAINING_ID, false);

        verify(courseSummaryRepository).findAllByTrainingIdAndStatusNotAndIsPastFalseOrderByDaysFromTodayAscCourseIdAsc(TRAINING_ID, "D");
        verify(courseSummaryRepository, never()).findAllByTrainingIdAndStatusNotOrderByIsPastAscDaysFromTodayAscCourseIdAsc(any(), any());
    }

    @Test
    void findTrainingCourses_includePastReturnsAllWithoutDeleted() {
        courseService.findTrainingCourses(TRAINING_ID, true);

        verify(courseSummaryRepository).findAllByTrainingIdAndStatusNotOrderByIsPastAscDaysFromTodayAscCourseIdAsc(TRAINING_ID, "D");
    }

    @Test
    void findTrainingCourses_deletedTrainingThrows() {
        when(trainingService.getValidActiveTrainingBy(14)).thenThrow(new PrimaryKeyNotFoundException("trainingId", 14));

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.findTrainingCourses(14, false));
        verifyNoInteractions(courseSummaryRepository);
    }

    @Test
    void getCourse_returnsCourseWithLecturersInOrder() {
        course.setRoom(createRoom(2));
        course.setNotes("Märge");
        CourseLecturer first = createCourseLecturer(8, "Meelis Teern");
        CourseLecturer second = createCourseLecturer(1, "Rain Tüür");
        when(courseLecturerRepository.findCourseLecturersBy(COURSE_ID)).thenReturn(List.of(first, second));

        CourseDto courseDto = courseService.getCourse(COURSE_ID);

        assertEquals(TRAINING_ID, courseDto.getTrainingId());
        assertEquals(2, courseDto.getRoomId());
        assertEquals("Märge", courseDto.getNotes());
        assertEquals(List.of(new LecturerDto(8, "Meelis Teern"), new LecturerDto(1, "Rain Tüür")), courseDto.getLecturers());
    }

    @Test
    void getCourse_deletedCourseThrows() {
        course.setStatus(CourseStatus.DELETED.getCode());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.getCourse(COURSE_ID));

        assertEquals("Ei leidnud primary keyd 'courseId' väärtusega: 4", exception.getMessage());
    }

    @Test
    void addCourse_savesCourseAndLecturersInOrder() {
        courseService.addCourse(TRAINING_ID, createCourseCreateRequestDto(LocalDate.of(2027, 1, 11), LocalDate.of(2027, 1, 15)));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertSame(training, savedCourse.getTraining());
        assertEquals(2, savedCourse.getRoom().getId());
        assertEquals("U", savedCourse.getStatus());
        assertEquals(new BigDecimal("490.00"), savedCourse.getPrice());
        assertNull(savedCourse.getNotes());
        assertNull(savedCourse.getMeetingLink());
        ArgumentCaptor<CourseLecturer> courseLecturerCaptor = ArgumentCaptor.forClass(CourseLecturer.class);
        verify(courseLecturerRepository, times(2)).save(courseLecturerCaptor.capture());
        assertEquals(List.of(1, 8), courseLecturerCaptor.getAllValues().stream().map(courseLecturer -> courseLecturer.getLecturer().getId()).toList());
        assertEquals(List.of(1, 2), courseLecturerCaptor.getAllValues().stream().map(CourseLecturer::getSortOrder).toList());
        verify(lecturerService).getValidAssignableLecturerBy(1, List.of());
    }

    @Test
    void addCourse_endBeforeStartThrowsAndSavesNothing() {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> courseService.addCourse(TRAINING_ID, createCourseCreateRequestDto(LocalDate.of(2027, 1, 15), LocalDate.of(2027, 1, 11))));

        assertEquals("COURSE_END_BEFORE_START", exception.getErrorCode());
        assertEquals("Lõppkuupäev ei saa olla varasem kui alguskuupäev", exception.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void addCourse_sameStartAndEndDateIsAllowed() {
        courseService.addCourse(TRAINING_ID, createCourseCreateRequestDto(LocalDate.of(2027, 1, 11), LocalDate.of(2027, 1, 11)));

        verify(courseRepository).save(any());
    }

    @Test
    void updateCourse_updatesFieldsAndReplacesLecturers() {
        when(courseLecturerRepository.findCourseLecturersBy(COURSE_ID)).thenReturn(List.of(createCourseLecturer(8, "Meelis Teern")));
        CourseUpdateRequestDto courseUpdateRequestDto = new CourseUpdateRequestDto(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 23),
                5, 40, new BigDecimal("490.00"), List.of(8), null, "X", "Tühistatud.", " ");

        courseService.updateCourse(COURSE_ID, courseUpdateRequestDto);

        assertEquals("X", course.getStatus());
        assertNull(course.getRoom());
        assertEquals("Tühistatud.", course.getNotes());
        assertNull(course.getMeetingLink());
        assertSame(training, course.getTraining());
        assertEquals(9, course.getCreatedBy().getId());
        InOrder inOrder = inOrder(courseLecturerRepository);
        inOrder.verify(courseLecturerRepository).deleteCourseLecturersBy(COURSE_ID);
        inOrder.verify(courseLecturerRepository).save(any());
        verify(lecturerService).getValidAssignableLecturerBy(8, List.of(8));
    }

    @Test
    void updateCourse_deletedCourseThrows() {
        course.setStatus(CourseStatus.DELETED.getCode());

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.updateCourse(COURSE_ID, new CourseUpdateRequestDto()));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void deleteCourse_setsDeletedStatus() {
        courseService.deleteCourse(COURSE_ID);

        assertEquals("D", course.getStatus());
        verify(courseRepository).save(course);
    }

    @Test
    void deleteCourse_alreadyDeletedChangesNothing() {
        course.setStatus(CourseStatus.DELETED.getCode());

        courseService.deleteCourse(COURSE_ID);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void deleteCourse_unknownCourseThrows() {
        when(courseRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseService.deleteCourse(123));
    }

    private static CourseCreateRequestDto createCourseCreateRequestDto(LocalDate startDate, LocalDate endDate) {
        return new CourseCreateRequestDto(1, startDate, endDate, 5, 40, new BigDecimal("490.00"), List.of(1, 8), 2, "U", "", "");
    }

    private CourseLecturer createCourseLecturer(Integer lecturerId, String fullName) {
        CourseLecturer courseLecturer = new CourseLecturer();
        courseLecturer.setCourse(course);
        courseLecturer.setLecturer(createLecturer(lecturerId, fullName));
        return courseLecturer;
    }

    private static Lecturer createLecturer(Integer lecturerId, String fullName) {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFullName(fullName);
        return lecturer;
    }

    private static Room createRoom(Integer roomId) {
        Room room = new Room();
        room.setId(roomId);
        return room;
    }
}
