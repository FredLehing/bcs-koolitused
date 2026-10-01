package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationSummaryDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantStatusDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantMapper;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantMapperImpl;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantRepository;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.profile.ProfileMapperImpl;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummary;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummaryMapper;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.adminregistrationsummary.AdminRegistrationSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseParticipantServiceTest {

    @Mock
    private CourseParticipantRepository courseParticipantRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private ParticipantService participantService;
    @Mock
    private ParticipantRepository participantRepository;
    @Spy
    private CourseParticipantMapper courseParticipantMapper = new CourseParticipantMapperImpl();
    @Spy
    private ProfileMapper profileMapper = new ProfileMapperImpl();
    @Mock
    private AdminRegistrationSummaryRepository adminRegistrationSummaryRepository;
    @Spy
    private AdminRegistrationSummaryMapper adminRegistrationSummaryMapper = new AdminRegistrationSummaryMapperImpl();

    @InjectMocks
    private CourseParticipantService courseParticipantService;

    @Test
    void findCourseParticipants_mapsParticipantAndProfileContacts() {
        when(courseParticipantRepository.findAllByCourseIdOrderByCreatedAtAsc(1)).thenReturn(List.of(createCourseParticipant()));

        List<CourseParticipantDto> courseParticipantDtos = courseParticipantService.findCourseParticipants(1);

        verify(courseService).getValidActiveCourseBy(1);
        CourseParticipantDto courseParticipantDto = courseParticipantDtos.getFirst();
        assertEquals(5, courseParticipantDto.getCourseParticipantId());
        assertEquals("Mari Lepp", courseParticipantDto.getParticipantName());
        assertEquals("mari.lepp@example.com", courseParticipantDto.getEmail());
        assertEquals("+37255005566", courseParticipantDto.getPhone());
        assertEquals(Instant.parse("2026-09-18T06:15:00Z"), courseParticipantDto.getRegisteredAt());
        assertEquals(false, courseParticipantDto.getHasPaid());
        assertEquals(true, courseParticipantDto.getRequiresLaptop());
        assertEquals("C", courseParticipantDto.getStatus());
        assertEquals("Loobus haiguse tõttu.", courseParticipantDto.getNotes());
    }

    @Test
    void findCourseParticipants_noParticipants_returnsEmptyList() {
        when(courseParticipantRepository.findAllByCourseIdOrderByCreatedAtAsc(2)).thenReturn(List.of());

        assertTrue(courseParticipantService.findCourseParticipants(2).isEmpty());
    }

    @Test
    void findCourseParticipants_unknownOrDeletedCourseThrows() {
        when(courseService.getValidActiveCourseBy(123)).thenThrow(new PrimaryKeyNotFoundException("courseId", 123));

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseParticipantService.findCourseParticipants(123));
        verify(courseParticipantRepository, never()).findAllByCourseIdOrderByCreatedAtAsc(any());
    }

    // ---------- registreerumise olek ----------

    @Test
    void getCourseParticipantStatus_returnsRowStatus() {
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(createParticipant()));
        when(courseParticipantRepository.findByCourseIdAndParticipantId(1, 1)).thenReturn(Optional.of(createCourseParticipantWithStatus("R")));

        assertEquals(new CourseParticipantStatusDto("R"), courseParticipantService.getCourseParticipantStatus(1, 2));
        verify(courseService).getValidCourseBy(1);
        verify(userService).getValidUserBy(2);
    }

    @Test
    void getCourseParticipantStatus_notRegisteredOrNoParticipant_returnsNull() {
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(createParticipant()));
        when(courseParticipantRepository.findByCourseIdAndParticipantId(9, 1)).thenReturn(Optional.empty());
        when(participantRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertNull(courseParticipantService.getCourseParticipantStatus(9, 2).getStatus());
        assertNull(courseParticipantService.getCourseParticipantStatus(9, 1).getStatus());
    }

    @Test
    void getCourseParticipantStatus_unknownUserThrows() {
        when(userService.getValidUserBy(123)).thenThrow(new PrimaryKeyNotFoundException("userId", 123));

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseParticipantService.getCourseParticipantStatus(1, 123));
    }

    // ---------- registreerumine ----------

    @Test
    void registerCourseParticipant_withoutParticipant_createsParticipantAndRegistration() {
        Course course = createCourse("O", LocalDate.now().plusDays(10));
        User user = createUser();
        Participant newParticipant = createParticipant();
        when(courseService.getValidPublicCourseBy(9)).thenReturn(course);
        when(userService.getValidUserBy(2)).thenReturn(user);
        when(participantRepository.findByUserId(2)).thenReturn(Optional.empty());
        when(participantService.addParticipant(eq(user), any(Profile.class))).thenReturn(newParticipant);

        courseParticipantService.registerCourseParticipant(9, createCourseRegistrationRequestDto(null, null));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(participantService).addParticipant(eq(user), profileCaptor.capture());
        assertEquals("anna.saar@example.com", profileCaptor.getValue().getEmail());
        CourseParticipant courseParticipant = captureSavedCourseParticipant();
        assertSame(course, courseParticipant.getCourse());
        assertSame(newParticipant, courseParticipant.getParticipant());
        assertEquals("R", courseParticipant.getStatus());
        assertFalse(courseParticipant.getHasPaid());
        assertFalse(courseParticipant.getRequiresLaptop());
        assertEquals("", courseParticipant.getNotes());
    }

    @Test
    void registerCourseParticipant_existingParticipant_updatesProfileAndName() {
        Participant participant = createParticipant();
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse("O", LocalDate.now()));
        when(userService.getValidUserBy(2)).thenReturn(createUser());
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(participant));
        when(courseParticipantRepository.findByCourseIdAndParticipantId(9, 1)).thenReturn(Optional.empty());

        courseParticipantService.registerCourseParticipant(9, createCourseRegistrationRequestDto(true, "Arve ettevõttele"));

        assertEquals("+37250000000", participant.getProfile().getPhone());
        verify(participantService).updateParticipantName(participant);
        verify(participantService, never()).addParticipant(any(), any());
        CourseParticipant courseParticipant = captureSavedCourseParticipant();
        assertTrue(courseParticipant.getRequiresLaptop());
        assertEquals("Arve ettevõttele", courseParticipant.getNotes());
    }

    @Test
    void registerCourseParticipant_cancelledRowBecomesRegistered() {
        CourseParticipant cancelledCourseParticipant = createCourseParticipantWithStatus("C");
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse("O", LocalDate.now().plusDays(3)));
        when(userService.getValidUserBy(2)).thenReturn(createUser());
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(createParticipant()));
        when(courseParticipantRepository.findByCourseIdAndParticipantId(9, 1)).thenReturn(Optional.of(cancelledCourseParticipant));

        courseParticipantService.registerCourseParticipant(9, createCourseRegistrationRequestDto(false, null));

        assertSame(cancelledCourseParticipant, captureSavedCourseParticipant());
        assertEquals("R", cancelledCourseParticipant.getStatus());
    }

    @Test
    void registerCourseParticipant_alreadyRegisteredThrows() {
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse("F", LocalDate.now().plusDays(3)));
        when(userService.getValidUserBy(2)).thenReturn(createUser());
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(createParticipant()));
        when(courseParticipantRepository.findByCourseIdAndParticipantId(9, 1)).thenReturn(Optional.of(createCourseParticipantWithStatus("R")));

        assertForbiddenWithoutSaving("ALREADY_REGISTERED", "Oled sellele toimumiskorrale juba registreerunud");
    }

    @Test
    void registerCourseParticipant_fullCourseThrows() {
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse("F", LocalDate.now().plusDays(3)));
        when(userService.getValidUserBy(2)).thenReturn(createUser());
        when(participantRepository.findByUserId(2)).thenReturn(Optional.empty());

        assertForbiddenWithoutSaving("COURSE_FULL", "Toimumiskord on täis");
    }

    @Test
    void registerCourseParticipant_startedCourseThrows() {
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse("O", LocalDate.now().minusDays(1)));
        when(userService.getValidUserBy(2)).thenReturn(createUser());
        when(participantRepository.findByUserId(2)).thenReturn(Optional.empty());

        assertForbiddenWithoutSaving("REGISTRATION_CLOSED", "Registreerumine on lõppenud");
    }

    @Test
    void registerCourseParticipant_nonPublicCourseThrows() {
        when(courseService.getValidPublicCourseBy(13)).thenThrow(new PrimaryKeyNotFoundException("courseId", 13));

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseParticipantService.registerCourseParticipant(13, createCourseRegistrationRequestDto(null, null)));
        verify(courseParticipantRepository, never()).save(any());
    }

    private void assertForbiddenWithoutSaving(String errorCode, String message) {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> courseParticipantService.registerCourseParticipant(9, createCourseRegistrationRequestDto(null, null)));
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        verify(courseParticipantRepository, never()).save(any());
        verify(participantService, never()).addParticipant(any(), any());
        verify(participantService, never()).updateParticipantName(any());
    }

    @Test
    void findAdminRegistrations_defaultReturnsOnlyRegisteredAndUpcoming() {
        courseParticipantService.findAdminRegistrations("et", null, null);

        verify(adminRegistrationSummaryRepository).findAdminRegistrationSummariesBy("et", false, false, "R", "D", "D");
    }

    @Test
    void findAdminRegistrations_includeCancelledAndPast() {
        courseParticipantService.findAdminRegistrations("en", true, true);

        verify(adminRegistrationSummaryRepository).findAdminRegistrationSummariesBy("en", true, true, "R", "D", "D");
    }

    @Test
    void findAdminRegistrations_mapsSummaryRow() {
        when(adminRegistrationSummaryRepository.findAdminRegistrationSummariesBy("et", false, false, "R", "D", "D"))
                .thenReturn(List.of(createAdminRegistrationSummary()));

        AdminRegistrationSummaryDto adminRegistrationSummaryDto = courseParticipantService.findAdminRegistrations("et", false, false).getFirst();

        assertEquals(1, adminRegistrationSummaryDto.getCourseParticipantId());
        assertEquals(Instant.parse("2026-09-10T09:00:00Z"), adminRegistrationSummaryDto.getRegisteredAt());
        assertEquals("Anna Saar", adminRegistrationSummaryDto.getParticipantName());
        assertEquals("anna.saar@example.com", adminRegistrationSummaryDto.getEmail());
        assertEquals(1, adminRegistrationSummaryDto.getCourseId());
        assertEquals("Java algkursus", adminRegistrationSummaryDto.getTrainingTitle());
        assertEquals(LocalDate.parse("2026-10-05"), adminRegistrationSummaryDto.getCourseStartDate());
        assertEquals(LocalDate.parse("2026-10-09"), adminRegistrationSummaryDto.getCourseEndDate());
        assertEquals(false, adminRegistrationSummaryDto.getIsPast());
        assertEquals(true, adminRegistrationSummaryDto.getHasPaid());
        assertEquals(true, adminRegistrationSummaryDto.getRequiresLaptop());
        assertEquals("R", adminRegistrationSummaryDto.getStatus());
    }

    @Test
    void getAdminRegistration_mapsParticipantAndCourse() {
        when(courseParticipantRepository.findById(1)).thenReturn(Optional.of(createCourseParticipantWithStatus("R")));
        when(adminRegistrationSummaryRepository.findByCourseParticipantIdAndContentLanguageCode(1, "et"))
                .thenReturn(Optional.of(createAdminRegistrationSummary()));

        AdminRegistrationDto adminRegistrationDto = courseParticipantService.getAdminRegistration(1, "et");

        assertEquals(1, adminRegistrationDto.getCourseParticipantId());
        assertEquals("R", adminRegistrationDto.getStatus());
        assertEquals("Registreerus veebilehe kaudu.", adminRegistrationDto.getNotes());
        assertNull(adminRegistrationDto.getAdminNotes());
        assertEquals(Instant.parse("2026-09-10T09:00:00Z"), adminRegistrationDto.getCreatedAt());
        assertEquals("+37256789012", adminRegistrationDto.getPhone());
        assertEquals("kasutaja@vali-it.ee", adminRegistrationDto.getAccountEmail());
        assertEquals("Java algkursus", adminRegistrationDto.getTrainingTitle());
        assertEquals("O", adminRegistrationDto.getCourseStatus());
        assertEquals(false, adminRegistrationDto.getIsPast());
    }

    @Test
    void getAdminRegistration_unknownCourseParticipantThrows() {
        when(courseParticipantRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> courseParticipantService.getAdminRegistration(123, "et"));

        assertEquals("Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123", exception.getMessage());
        verify(adminRegistrationSummaryRepository, never()).findByCourseParticipantIdAndContentLanguageCode(any(), any());
    }

    @Test
    void getAdminRegistration_unknownContentLangThrows() {
        when(courseParticipantRepository.findById(1)).thenReturn(Optional.of(createCourseParticipantWithStatus("R")));
        when(adminRegistrationSummaryRepository.findByCourseParticipantIdAndContentLanguageCode(1, "xx")).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> courseParticipantService.getAdminRegistration(1, "xx"));
    }

    private CourseParticipant captureSavedCourseParticipant() {
        ArgumentCaptor<CourseParticipant> courseParticipantCaptor = ArgumentCaptor.forClass(CourseParticipant.class);
        verify(courseParticipantRepository).save(courseParticipantCaptor.capture());
        return courseParticipantCaptor.getValue();
    }

    // 3_import.sql course_participant 1 (Anna Saar, toimumiskord 1); konto e-post erineb profiili omast
    private static AdminRegistrationSummary createAdminRegistrationSummary() {
        AdminRegistrationSummary adminRegistrationSummary = new AdminRegistrationSummary();
        ReflectionTestUtils.setField(adminRegistrationSummary, "courseParticipantId", 1);
        ReflectionTestUtils.setField(adminRegistrationSummary, "contentLanguageCode", "et");
        ReflectionTestUtils.setField(adminRegistrationSummary, "status", "R");
        ReflectionTestUtils.setField(adminRegistrationSummary, "hasPaid", true);
        ReflectionTestUtils.setField(adminRegistrationSummary, "requiresLaptop", true);
        ReflectionTestUtils.setField(adminRegistrationSummary, "notes", "Registreerus veebilehe kaudu.");
        ReflectionTestUtils.setField(adminRegistrationSummary, "createdAt", Instant.parse("2026-09-10T09:00:00Z"));
        ReflectionTestUtils.setField(adminRegistrationSummary, "updatedAt", Instant.parse("2026-09-10T09:00:00Z"));
        ReflectionTestUtils.setField(adminRegistrationSummary, "participantName", "Anna Saar");
        ReflectionTestUtils.setField(adminRegistrationSummary, "email", "anna.saar@example.com");
        ReflectionTestUtils.setField(adminRegistrationSummary, "phone", "+37256789012");
        ReflectionTestUtils.setField(adminRegistrationSummary, "accountEmail", "kasutaja@vali-it.ee");
        ReflectionTestUtils.setField(adminRegistrationSummary, "courseId", 1);
        ReflectionTestUtils.setField(adminRegistrationSummary, "trainingTitle", "Java algkursus");
        ReflectionTestUtils.setField(adminRegistrationSummary, "courseStartDate", LocalDate.parse("2026-10-05"));
        ReflectionTestUtils.setField(adminRegistrationSummary, "courseEndDate", LocalDate.parse("2026-10-09"));
        ReflectionTestUtils.setField(adminRegistrationSummary, "courseStatus", "O");
        ReflectionTestUtils.setField(adminRegistrationSummary, "isPast", false);
        return adminRegistrationSummary;
    }

    private static CourseRegistrationRequestDto createCourseRegistrationRequestDto(Boolean requiresLaptop, String notes) {
        return new CourseRegistrationRequestDto(2, "Anna", "Saar", "anna.saar@example.com", "+37250000000", requiresLaptop, notes);
    }

    private static Course createCourse(String status, LocalDate startDate) {
        Course course = new Course();
        course.setId(9);
        course.setStatus(status);
        course.setStartDate(startDate);
        return course;
    }

    private static User createUser() {
        User user = new User();
        user.setId(2);
        return user;
    }

    // 3_import.sql osaleja 1 (kasutaja 2)
    private static Participant createParticipant() {
        Profile profile = new Profile();
        profile.setFirstName("Anna");
        profile.setLastName("Saar");
        profile.setEmail("anna.saar@example.com");
        profile.setPhone("+37256789012");
        Participant participant = new Participant();
        participant.setId(1);
        participant.setProfile(profile);
        return participant;
    }

    private static CourseParticipant createCourseParticipantWithStatus(String status) {
        CourseParticipant courseParticipant = new CourseParticipant();
        courseParticipant.setStatus(status);
        return courseParticipant;
    }

    // 3_import.sql course_participant 5 (toimumiskord 1, loobunud)
    private static CourseParticipant createCourseParticipant() {
        Profile profile = new Profile();
        profile.setEmail("mari.lepp@example.com");
        profile.setPhone("+37255005566");
        Participant participant = new Participant();
        participant.setName("Mari Lepp");
        participant.setProfile(profile);
        CourseParticipant courseParticipant = new CourseParticipant();
        courseParticipant.setId(5);
        courseParticipant.setParticipant(participant);
        courseParticipant.setCreatedAt(Instant.parse("2026-09-18T06:15:00Z"));
        courseParticipant.setHasPaid(false);
        courseParticipant.setRequiresLaptop(true);
        courseParticipant.setStatus("C");
        courseParticipant.setNotes("Loobus haiguse tõttu.");
        return courseParticipant;
    }
}
