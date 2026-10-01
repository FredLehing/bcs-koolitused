package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.enquiry.dto.CourseEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.EnquiryCreateRequestDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.enquiry.Enquiry;
import ee.bcskoolitus.persistance.enquiry.EnquiryMapper;
import ee.bcskoolitus.persistance.enquiry.EnquiryMapperImpl;
import ee.bcskoolitus.persistance.enquiry.EnquiryRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.profile.ProfileMapperImpl;
import ee.bcskoolitus.persistance.profile.ProfileRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryMapper;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryMapperImpl;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnquiryServiceTest {

    @Mock
    private EnquiryRepository enquiryRepository;
    @Mock
    private AdminEnquirySummaryRepository adminEnquirySummaryRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private ProfileRepository profileRepository;
    @Spy
    private ProfileMapper profileMapper = new ProfileMapperImpl();
    @Spy
    private AdminEnquirySummaryMapper adminEnquirySummaryMapper = new AdminEnquirySummaryMapperImpl();
    @Spy
    private EnquiryMapper enquiryMapper = new EnquiryMapperImpl();

    @InjectMocks
    private EnquiryService enquiryService;

    @Test
    void findAdminEnquiries_defaultReturnsOnlyNew() {
        enquiryService.findAdminEnquiries("et", false);

        verify(adminEnquirySummaryRepository).findAllByContentLanguageCodeAndStatusOrderByCreatedAtDescEnquiryIdDesc("et", "U");
        verify(adminEnquirySummaryRepository, never()).findAllByContentLanguageCodeOrderByCreatedAtDescEnquiryIdDesc(any());
    }

    @Test
    void findAdminEnquiries_includeHandledReturnsAll() {
        enquiryService.findAdminEnquiries("en", true);

        verify(adminEnquirySummaryRepository).findAllByContentLanguageCodeOrderByCreatedAtDescEnquiryIdDesc("en");
    }

    @Test
    void getAdminEnquiry_unknownEnquiryThrows() {
        when(enquiryRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> enquiryService.getAdminEnquiry(123, "et"));

        assertEquals("Ei leidnud primary keyd 'enquiryId' väärtusega: 123", exception.getMessage());
        verify(adminEnquirySummaryRepository, never()).findByEnquiryIdAndContentLanguageCode(any(), any());
    }

    @Test
    void getAdminEnquiry_unknownContentLangThrows() {
        when(enquiryRepository.findById(4)).thenReturn(Optional.of(createEnquiry(4, "U")));
        when(adminEnquirySummaryRepository.findByEnquiryIdAndContentLanguageCode(4, "xx")).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> enquiryService.getAdminEnquiry(4, "xx"));
    }

    @Test
    void handleEnquiry_setsHandledStatus() {
        Enquiry enquiry = createEnquiry(4, "U");
        when(enquiryRepository.findById(4)).thenReturn(Optional.of(enquiry));

        enquiryService.handleEnquiry(4);

        assertEquals("H", enquiry.getStatus());
        verify(enquiryRepository).save(enquiry);
    }

    @Test
    void handleEnquiry_alreadyHandledChangesNothing() {
        when(enquiryRepository.findById(3)).thenReturn(Optional.of(createEnquiry(3, "H")));

        enquiryService.handleEnquiry(3);

        verify(enquiryRepository, never()).save(any());
    }

    @Test
    void reopenEnquiry_setsNewStatus() {
        Enquiry enquiry = createEnquiry(3, "H");
        when(enquiryRepository.findById(3)).thenReturn(Optional.of(enquiry));

        enquiryService.reopenEnquiry(3);

        assertEquals("U", enquiry.getStatus());
        verify(enquiryRepository).save(enquiry);
    }

    @Test
    void reopenEnquiry_unknownEnquiryThrows() {
        when(enquiryRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(PrimaryKeyNotFoundException.class, () -> enquiryService.reopenEnquiry(123));
        verify(enquiryRepository, never()).save(any());
    }

    @Test
    void findCourseEnquiries_mapsProfileContacts() {
        Profile profile = new Profile();
        profile.setFirstName("Martin");
        profile.setLastName("Kask");
        profile.setEmail("martin.kask@example.com");
        Enquiry enquiry = createEnquiry(4, "U");
        enquiry.setProfile(profile);
        enquiry.setCreatedAt(Instant.parse("2026-09-28T13:40:00Z"));
        when(enquiryRepository.findAllByCourseIdOrderByCreatedAtDescIdDesc(5)).thenReturn(List.of(enquiry));

        List<CourseEnquiryDto> courseEnquiryDtos = enquiryService.findCourseEnquiries(5);

        verify(courseService).getValidCourseBy(5);
        assertEquals(List.of(new CourseEnquiryDto(4, Instant.parse("2026-09-28T13:40:00Z"), "Martin Kask", "martin.kask@example.com", null, "U")), courseEnquiryDtos);
    }

    @Test
    void findCourseEnquiries_unknownCourseThrows() {
        when(courseService.getValidCourseBy(123)).thenThrow(new PrimaryKeyNotFoundException("courseId", 123));

        assertThrows(PrimaryKeyNotFoundException.class, () -> enquiryService.findCourseEnquiries(123));
        verify(enquiryRepository, never()).findAllByCourseIdOrderByCreatedAtDescIdDesc(any());
    }

    @Test
    void addEnquiry_createsNewProfileAndNewEnquiry() {
        Training training = createTraining(1);
        Course course = createCourse(1, training);
        when(trainingService.getValidPublishedTrainingBy(1)).thenReturn(training);
        when(courseService.getValidPublicCourseBy(1)).thenReturn(course);
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        enquiryService.addEnquiry(createEnquiryCreateRequestDto(1, " "));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertEquals("Kati", profileCaptor.getValue().getFirstName());
        assertEquals("Karu", profileCaptor.getValue().getLastName());
        assertEquals("kati.karu@example.com", profileCaptor.getValue().getEmail());
        assertEquals("+37255512300", profileCaptor.getValue().getPhone());
        ArgumentCaptor<Enquiry> enquiryCaptor = ArgumentCaptor.forClass(Enquiry.class);
        verify(enquiryRepository).save(enquiryCaptor.capture());
        Enquiry enquiry = enquiryCaptor.getValue();
        assertSame(training, enquiry.getTraining());
        assertSame(course, enquiry.getCourse());
        assertSame(profileCaptor.getValue(), enquiry.getProfile());
        assertNull(enquiry.getCompanyName());
        assertEquals("Kas kursusele saab tulla ka ilma eelteadmisteta?", enquiry.getMessage());
        assertEquals("U", enquiry.getStatus());
    }

    @Test
    void addEnquiry_withoutCourse_savesEnquiryWithoutCourse() {
        when(trainingService.getValidPublishedTrainingBy(1)).thenReturn(createTraining(1));

        enquiryService.addEnquiry(createEnquiryCreateRequestDto(null, "OÜ Näidisfirma"));

        ArgumentCaptor<Enquiry> enquiryCaptor = ArgumentCaptor.forClass(Enquiry.class);
        verify(enquiryRepository).save(enquiryCaptor.capture());
        assertNull(enquiryCaptor.getValue().getCourse());
        assertEquals("OÜ Näidisfirma", enquiryCaptor.getValue().getCompanyName());
        verify(courseService, never()).getValidPublicCourseBy(any());
    }

    @Test
    void addEnquiry_courseOfAnotherTrainingThrows() {
        when(trainingService.getValidPublishedTrainingBy(1)).thenReturn(createTraining(1));
        when(courseService.getValidPublicCourseBy(9)).thenReturn(createCourse(9, createTraining(3)));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> enquiryService.addEnquiry(createEnquiryCreateRequestDto(9, null)));

        assertEquals("Ei leidnud primary keyd 'courseId' väärtusega: 9", exception.getMessage());
        verify(profileRepository, never()).save(any());
        verify(enquiryRepository, never()).save(any());
    }

    @Test
    void addEnquiry_unpublishedTrainingThrows() {
        when(trainingService.getValidPublishedTrainingBy(1)).thenThrow(new PrimaryKeyNotFoundException("trainingId", 1));

        assertThrows(PrimaryKeyNotFoundException.class, () -> enquiryService.addEnquiry(createEnquiryCreateRequestDto(1, null)));
        verify(enquiryRepository, never()).save(any());
    }

    private static EnquiryCreateRequestDto createEnquiryCreateRequestDto(Integer courseId, String companyName) {
        return new EnquiryCreateRequestDto(1, courseId, "Kati", "Karu", "kati.karu@example.com", "+37255512300",
                companyName, "Kas kursusele saab tulla ka ilma eelteadmisteta?");
    }

    private static Training createTraining(Integer trainingId) {
        Training training = new Training();
        training.setId(trainingId);
        return training;
    }

    private static Course createCourse(Integer courseId, Training training) {
        Course course = new Course();
        course.setId(courseId);
        course.setTraining(training);
        return course;
    }

    private static Enquiry createEnquiry(Integer enquiryId, String status) {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(enquiryId);
        enquiry.setStatus(status);
        return enquiry;
    }
}
