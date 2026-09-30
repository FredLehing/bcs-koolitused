package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.CourseUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.CourseMapper;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapper;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummary;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryMapper;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static ee.bcskoolitus.Error.COURSE_END_BEFORE_START;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseLecturerRepository courseLecturerRepository;
    private final CourseLecturerMapper courseLecturerMapper;
    private final CourseSummaryRepository courseSummaryRepository;
    private final CourseSummaryMapper courseSummaryMapper;
    private final TrainingService trainingService;
    private final LecturerService lecturerService;
    private final RoomService roomService;
    private final UserService userService;

    // Koolituse kalender: kustutatud (D) välja jäetud; vaikimisi ainult tulevased (end_date >= täna)
    public List<CourseSummaryDto> findTrainingCourses(Integer trainingId, Boolean includePast) {
        trainingService.getValidActiveTrainingBy(trainingId);
        String deletedStatus = CourseStatus.DELETED.getCode();
        List<CourseSummary> courseSummaries = Boolean.TRUE.equals(includePast)
                ? courseSummaryRepository.findAllByTrainingIdAndStatusNotOrderByIsPastAscDaysFromTodayAscCourseIdAsc(trainingId, deletedStatus)
                : courseSummaryRepository.findAllByTrainingIdAndStatusNotAndIsPastFalseOrderByDaysFromTodayAscCourseIdAsc(trainingId, deletedStatus);
        return courseSummaryMapper.toCourseSummaryDtos(courseSummaries);
    }

    // Leiab ka kustutatud toimumiskorra — kasutab delete
    public Course getValidCourseBy(Integer courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseId", courseId));
    }

    // Kustutatud toimumiskord (status D) on nagu olematu → 404
    public Course getValidActiveCourseBy(Integer courseId) {
        Course course = getValidCourseBy(courseId);
        if (CourseStatus.DELETED.getCode().equals(course.getStatus())) {
            throw new PrimaryKeyNotFoundException("courseId", courseId);
        }
        return course;
    }

    @Transactional(readOnly = true)
    public CourseDto getCourse(Integer courseId) {
        Course course = getValidActiveCourseBy(courseId);
        CourseDto courseDto = courseMapper.toCourseDto(course);
        courseDto.setLecturers(courseLecturerMapper.toLecturerDtos(courseLecturerRepository.findCourseLecturersBy(courseId)));
        return courseDto;
    }

    // Loob toimumiskorra ja koolitajate seosed ühes transaktsioonis (ka mustandis koolitusele)
    @Transactional
    public void addCourse(Integer trainingId, CourseCreateRequestDto courseCreateRequestDto) {
        validateEndDateNotBeforeStartDate(courseCreateRequestDto.getStartDate(), courseCreateRequestDto.getEndDate());
        Course course = courseMapper.toCourse(courseCreateRequestDto);
        course.setTraining(trainingService.getValidActiveTrainingBy(trainingId));
        course.setCreatedBy(userService.getValidUserBy(courseCreateRequestDto.getUserId()));
        course.setRoom(getRoomOrNull(courseCreateRequestDto.getRoomId()));
        course.setNotes(blankToNull(courseCreateRequestDto.getNotes()));
        course.setMeetingLink(blankToNull(courseCreateRequestDto.getMeetingLink()));
        courseRepository.save(course);
        addCourseLecturers(course, courseCreateRequestDto.getLecturerIds(), List.of());
    }

    // Muudab kõik väljad (ka staatuse) ja kirjutab koolitajad üle ühes transaktsioonis
    @Transactional
    public void updateCourse(Integer courseId, CourseUpdateRequestDto courseUpdateRequestDto) {
        Course course = getValidActiveCourseBy(courseId);
        validateEndDateNotBeforeStartDate(courseUpdateRequestDto.getStartDate(), courseUpdateRequestDto.getEndDate());
        courseMapper.updateCourse(courseUpdateRequestDto, course);
        course.setRoom(getRoomOrNull(courseUpdateRequestDto.getRoomId()));
        course.setNotes(blankToNull(courseUpdateRequestDto.getNotes()));
        course.setMeetingLink(blankToNull(courseUpdateRequestDto.getMeetingLink()));
        courseRepository.save(course);
        replaceCourseLecturers(course, courseUpdateRequestDto.getLecturerIds());
    }

    // Soft delete: status = D; osalejad ja koolitajad jäävad alles. Korduv kustutamine ei muuda midagi.
    @Transactional
    public void deleteCourse(Integer courseId) {
        Course course = getValidCourseBy(courseId);
        if (CourseStatus.DELETED.getCode().equals(course.getStatus())) {
            return;
        }
        course.setStatus(CourseStatus.DELETED.getCode());
        courseRepository.save(course);
    }

    private static void validateEndDateNotBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ForbiddenException(COURSE_END_BEFORE_START.getMessage(), COURSE_END_BEFORE_START.name());
        }
    }

    private Room getRoomOrNull(Integer roomId) {
        return roomId == null ? null : roomService.getValidRoomBy(roomId);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    // Praegused seosed loetakse enne kustutamist — nende koolitajad võivad jääda ka kustutatuna
    private void replaceCourseLecturers(Course course, List<Integer> lecturerIds) {
        List<Integer> linkedLecturerIds = courseLecturerRepository.findCourseLecturersBy(course.getId()).stream()
                .map(courseLecturer -> courseLecturer.getLecturer().getId())
                .toList();
        courseLecturerRepository.deleteCourseLecturersBy(course.getId());
        addCourseLecturers(course, lecturerIds, linkedLecturerIds);
    }

    // Järjekord listis = sort_order (1 = esimene)
    private void addCourseLecturers(Course course, List<Integer> lecturerIds, List<Integer> linkedLecturerIds) {
        int sortOrder = 1;
        for (Integer lecturerId : lecturerIds) {
            CourseLecturer courseLecturer = new CourseLecturer();
            courseLecturer.setCourse(course);
            courseLecturer.setLecturer(lecturerService.getValidAssignableLecturerBy(lecturerId, linkedLecturerIds));
            courseLecturer.setSortOrder(sortOrder++);
            courseLecturerRepository.save(courseLecturer);
        }
    }
}
