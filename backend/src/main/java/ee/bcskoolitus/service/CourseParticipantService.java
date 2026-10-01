package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseParticipantStatus;
import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantStatusDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantMapper;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantRepository;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ee.bcskoolitus.Error.ALREADY_REGISTERED;
import static ee.bcskoolitus.Error.COURSE_FULL;
import static ee.bcskoolitus.Error.REGISTRATION_CLOSED;

@Service
@RequiredArgsConstructor
public class CourseParticipantService {

    private final CourseParticipantRepository courseParticipantRepository;
    private final CourseParticipantMapper courseParticipantMapper;
    private final ParticipantRepository participantRepository;
    private final ProfileMapper profileMapper;
    private final CourseService courseService;
    private final UserService userService;
    private final ParticipantService participantService;

    // Toimumiskorra osalejad registreerumise järjekorras (ka loobunud); kontaktandmed profiilist
    @Transactional(readOnly = true)
    public List<CourseParticipantDto> findCourseParticipants(Integer courseId) {
        courseService.getValidActiveCourseBy(courseId);
        List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourseIdOrderByCreatedAtAsc(courseId);
        return courseParticipantMapper.toCourseParticipantDtos(courseParticipants);
    }

    // Kasutaja registreerumise olek: R / C; null = pole registreerunud (ka siis, kui kasutajal osalejat pole)
    @Transactional(readOnly = true)
    public CourseParticipantStatusDto getCourseParticipantStatus(Integer courseId, Integer userId) {
        courseService.getValidCourseBy(courseId);
        userService.getValidUserBy(userId);
        String status = findUserCourseParticipant(courseId, userId)
                .map(CourseParticipant::getStatus)
                .orElse(null);
        return new CourseParticipantStatusDto(status);
    }

    // Kasutaja registreerib iseennast: vajadusel luuakse osaleja, olemasoleva osaleja profiil ja nimi uuenevad;
    // loobunu (C) rida muudetakse tagasi registreerunuks. Kõik kontrollid enne salvestamist.
    @Transactional
    public void registerCourseParticipant(Integer courseId, CourseRegistrationRequestDto courseRegistrationRequestDto) {
        Course course = courseService.getValidPublicCourseBy(courseId);
        User user = userService.getValidUserBy(courseRegistrationRequestDto.getUserId());
        Optional<CourseParticipant> existingCourseParticipant = findUserCourseParticipant(courseId, user.getId());
        validateRegistrationAllowed(course, existingCourseParticipant);
        Participant participant = addOrUpdateParticipant(user, courseRegistrationRequestDto);
        CourseParticipant courseParticipant = existingCourseParticipant.orElseGet(CourseParticipant::new);
        courseParticipant.setCourse(course);
        courseParticipant.setParticipant(participant);
        courseParticipant.setStatus(CourseParticipantStatus.REGISTERED.getCode());
        courseParticipant.setHasPaid(false);
        courseParticipant.setRequiresLaptop(Boolean.TRUE.equals(courseRegistrationRequestDto.getRequiresLaptop()));
        courseParticipant.setNotes(courseRegistrationRequestDto.getNotes() == null ? "" : courseRegistrationRequestDto.getNotes());
        courseParticipantRepository.save(courseParticipant);
    }

    private Optional<CourseParticipant> findUserCourseParticipant(Integer courseId, Integer userId) {
        return participantRepository.findByUserId(userId)
                .flatMap(participant -> courseParticipantRepository.findByCourseIdAndParticipantId(courseId, participant.getId()));
    }

    // Järjekord: registreerumine lõppenud → juba registreerunud → täis
    private static void validateRegistrationAllowed(Course course, Optional<CourseParticipant> existingCourseParticipant) {
        if (course.getStartDate().isBefore(LocalDate.now())) {
            throw new ForbiddenException(REGISTRATION_CLOSED.getMessage(), REGISTRATION_CLOSED.name());
        }
        boolean isRegistered = existingCourseParticipant
                .map(courseParticipant -> CourseParticipantStatus.REGISTERED.getCode().equals(courseParticipant.getStatus()))
                .orElse(false);
        if (isRegistered) {
            throw new ForbiddenException(ALREADY_REGISTERED.getMessage(), ALREADY_REGISTERED.name());
        }
        if (CourseStatus.FULL.getCode().equals(course.getStatus())) {
            throw new ForbiddenException(COURSE_FULL.getMessage(), COURSE_FULL.name());
        }
    }

    private Participant addOrUpdateParticipant(User user, CourseRegistrationRequestDto courseRegistrationRequestDto) {
        Optional<Participant> existingParticipant = participantRepository.findByUserId(user.getId());
        if (existingParticipant.isEmpty()) {
            return participantService.addParticipant(user, profileMapper.toProfile(courseRegistrationRequestDto));
        }
        Participant participant = existingParticipant.get();
        profileMapper.updateProfile(courseRegistrationRequestDto, participant.getProfile());
        participantService.updateParticipantName(participant);
        return participant;
    }
}
