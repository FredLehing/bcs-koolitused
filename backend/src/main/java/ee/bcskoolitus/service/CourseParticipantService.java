package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseParticipantStatus;
import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationSummaryDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationUpdateRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantStatusDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.MyRegistrationDto;
import ee.bcskoolitus.infrastructure.exception.DataNotFoundException;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantMapper;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantRepository;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummary;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummaryMapper;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ee.bcskoolitus.Error.ALREADY_REGISTERED;
import static ee.bcskoolitus.Error.CANCEL_NOT_ALLOWED;
import static ee.bcskoolitus.Error.COURSE_FULL;
import static ee.bcskoolitus.Error.REGISTRATION_CLOSED;
import static ee.bcskoolitus.Error.REGISTRATION_NOT_FOUND;

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
    private final AdminRegistrationSummaryRepository adminRegistrationSummaryRepository;
    private final AdminRegistrationSummaryMapper adminRegistrationSummaryMapper;
    private final TrainingTranslationService trainingTranslationService;

    public CourseParticipant getValidCourseParticipantBy(Integer courseParticipantId) {
        return courseParticipantRepository.findById(courseParticipantId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseParticipantId", courseParticipantId));
    }

    // Toimumiskorra osalejad registreerumise järjekorras (ka loobunud); kontaktandmed profiilist
    @Transactional(readOnly = true)
    public List<CourseParticipantDto> findCourseParticipants(Integer courseId) {
        courseService.getValidActiveCourseBy(courseId);
        List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourseIdOrderByCreatedAtAsc(courseId);
        return courseParticipantMapper.toCourseParticipantDtos(courseParticipants);
    }

    // Admini nimekiri: uusimad eespool; vaikimisi ainult registreerunud (R) ja toimumiskorrad, mis pole lõppenud;
    // kustutatud toimumiskorrad ja koolitused alati välja
    @Transactional(readOnly = true)
    public List<AdminRegistrationSummaryDto> findAdminRegistrations(String contentLang, Boolean includeCancelled, Boolean includePast) {
        List<AdminRegistrationSummary> adminRegistrationSummaries = adminRegistrationSummaryRepository.findAdminRegistrationSummariesBy(
                contentLang, Boolean.TRUE.equals(includeCancelled), Boolean.TRUE.equals(includePast),
                CourseParticipantStatus.REGISTERED.getCode(), CourseStatus.DELETED.getCode(), TrainingStatus.DELETED.getCode());
        return adminRegistrationSummaryMapper.toAdminRegistrationSummaryDtos(adminRegistrationSummaries);
    }

    // Üks registreerumine koos osaleja ja toimumiskorraga (ka kustutatud toimumiskorra oma); tundmatu contentLang → 404
    @Transactional(readOnly = true)
    public AdminRegistrationDto getAdminRegistration(Integer courseParticipantId, String contentLang) {
        getValidCourseParticipantBy(courseParticipantId);
        AdminRegistrationSummary adminRegistrationSummary = adminRegistrationSummaryRepository
                .findByCourseParticipantIdAndContentLanguageCode(courseParticipantId, contentLang)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseParticipantId", courseParticipantId));
        return adminRegistrationSummaryMapper.toAdminRegistrationDto(adminRegistrationSummary);
    }

    // Admin muudab ainult registreerumise enda välju (osaleja lisainfo ja profiil jäävad); kustutamist pole — loobumine on status C.
    // Taastamine (C → R) on lubatud ka täis toimumiskorrale, sest mahutavust veel ei modelleerita
    @Transactional
    public void updateAdminRegistration(Integer courseParticipantId, AdminRegistrationUpdateRequestDto adminRegistrationUpdateRequestDto) {
        CourseParticipant courseParticipant = getValidCourseParticipantBy(courseParticipantId);
        courseParticipantMapper.updateCourseParticipant(adminRegistrationUpdateRequestDto, courseParticipant);
        courseParticipant.setAdminNotes(trimToNull(adminRegistrationUpdateRequestDto.getAdminNotes()));
        courseParticipantRepository.save(courseParticipant);
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

    // "Minu koolitused": kasutaja osaleja registreerumised alguse järgi (ka loobunud ja toimunud); osalejata kasutajal tühi list
    @Transactional(readOnly = true)
    public List<MyRegistrationDto> findMyRegistrations(Integer userId, String contentLang) {
        userService.getValidUserBy(userId);
        Optional<Participant> participant = participantRepository.findByUserId(userId);
        if (participant.isEmpty()) {
            return List.of();
        }
        List<CourseParticipant> courseParticipants = courseParticipantRepository.findParticipantCourseParticipantsBy(
                participant.get().getId(), CourseStatus.DELETED.getCode());
        return courseParticipants.stream()
                .map(courseParticipant -> createMyRegistrationDto(courseParticipant, contentLang))
                .toList();
    }

    // Kasutaja loobub ise: ainult oma registreerumisest, mis on R ja mille toimumiskord pole alanud ega tühistatud
    @Transactional
    public void cancelMyRegistration(Integer userId, Integer courseParticipantId) {
        userService.getValidUserBy(userId);
        CourseParticipant courseParticipant = getValidCourseParticipantBy(courseParticipantId);
        if (!courseParticipant.getParticipant().getUser().getId().equals(userId)) {
            throw new DataNotFoundException(REGISTRATION_NOT_FOUND.getMessage(), REGISTRATION_NOT_FOUND.name());
        }
        if (!isCancelAllowed(courseParticipant)) {
            throw new ForbiddenException(CANCEL_NOT_ALLOWED.getMessage(), CANCEL_NOT_ALLOWED.name());
        }
        courseParticipant.setStatus(CourseParticipantStatus.CANCELLED.getCode());
        courseParticipantRepository.save(courseParticipant);
    }

    private MyRegistrationDto createMyRegistrationDto(CourseParticipant courseParticipant, String contentLang) {
        MyRegistrationDto myRegistrationDto = courseParticipantMapper.toMyRegistrationDto(courseParticipant);
        myRegistrationDto.setTrainingTitle(
                trainingTranslationService.getTrainingTitle(courseParticipant.getCourse().getTraining().getId(), contentLang));
        myRegistrationDto.setCanCancel(isCancelAllowed(courseParticipant));
        return myRegistrationDto;
    }

    private static boolean isCancelAllowed(CourseParticipant courseParticipant) {
        Course course = courseParticipant.getCourse();
        return CourseParticipantStatus.REGISTERED.getCode().equals(courseParticipant.getStatus())
                && course.getStartDate().isAfter(LocalDate.now())
                && !CourseStatus.CANCELLED.getCode().equals(course.getStatus())
                && !CourseStatus.DELETED.getCode().equals(course.getStatus());
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
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
