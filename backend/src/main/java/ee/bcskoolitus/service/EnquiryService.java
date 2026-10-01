package ee.bcskoolitus.service;

import ee.bcskoolitus.EnquiryStatus;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.CourseEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.EnquiryCreateRequestDto;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquirySummaryDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.enquiry.Enquiry;
import ee.bcskoolitus.persistance.enquiry.EnquiryMapper;
import ee.bcskoolitus.persistance.enquiry.EnquiryRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.profile.ProfileRepository;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummary;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryMapper;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final AdminEnquirySummaryRepository adminEnquirySummaryRepository;
    private final AdminEnquirySummaryMapper adminEnquirySummaryMapper;
    private final EnquiryMapper enquiryMapper;
    private final CourseService courseService;
    private final TrainingService trainingService;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public Enquiry getValidEnquiryBy(Integer enquiryId) {
        return enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("enquiryId", enquiryId));
    }

    // Admini nimekiri: uusimad eespool; vaikimisi ainult uued (status U)
    public List<AdminEnquirySummaryDto> findAdminEnquiries(String contentLang, Boolean includeHandled) {
        List<AdminEnquirySummary> adminEnquirySummaries = Boolean.TRUE.equals(includeHandled)
                ? adminEnquirySummaryRepository.findAllByContentLanguageCodeOrderByCreatedAtDescEnquiryIdDesc(contentLang)
                : adminEnquirySummaryRepository.findAllByContentLanguageCodeAndStatusOrderByCreatedAtDescEnquiryIdDesc(contentLang, EnquiryStatus.NEW.getCode());
        return adminEnquirySummaryMapper.toAdminEnquirySummaryDtos(adminEnquirySummaries);
    }

    // Toimumiskorraga seotud päringud (kõik staatused), uusimad eespool
    @Transactional(readOnly = true)
    public List<CourseEnquiryDto> findCourseEnquiries(Integer courseId) {
        courseService.getValidCourseBy(courseId);
        List<Enquiry> enquiries = enquiryRepository.findAllByCourseIdOrderByCreatedAtDescIdDesc(courseId);
        return enquiryMapper.toCourseEnquiryDtos(enquiries);
    }

    // Külastaja päring: iga päring loob uue profiili (e-posti järgi ei otsita) ja uue päringu (status U)
    @Transactional
    public void addEnquiry(EnquiryCreateRequestDto enquiryCreateRequestDto) {
        Training training = trainingService.getValidPublishedTrainingBy(enquiryCreateRequestDto.getTrainingId());
        Course course = getTrainingPublicCourseOrNull(enquiryCreateRequestDto.getCourseId(), training.getId());
        Profile profile = profileRepository.save(profileMapper.toProfile(enquiryCreateRequestDto));
        Enquiry enquiry = new Enquiry();
        enquiry.setTraining(training);
        enquiry.setCourse(course);
        enquiry.setProfile(profile);
        enquiry.setCompanyName(blankToNull(enquiryCreateRequestDto.getCompanyName()));
        enquiry.setMessage(enquiryCreateRequestDto.getMessage());
        enquiry.setStatus(EnquiryStatus.NEW.getCode());
        enquiryRepository.save(enquiry);
    }

    // Toimumiskord peab olema avalik ja kuuluma samale koolitusele, muidu nagu olematu → 404
    private Course getTrainingPublicCourseOrNull(Integer courseId, Integer trainingId) {
        if (courseId == null) {
            return null;
        }
        Course course = courseService.getValidPublicCourseBy(courseId);
        if (!course.getTraining().getId().equals(trainingId)) {
            throw new PrimaryKeyNotFoundException("courseId", courseId);
        }
        return course;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public AdminEnquiryDto getAdminEnquiry(Integer enquiryId, String contentLang) {
        getValidEnquiryBy(enquiryId);
        AdminEnquirySummary adminEnquirySummary = adminEnquirySummaryRepository.findByEnquiryIdAndContentLanguageCode(enquiryId, contentLang)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("enquiryId", enquiryId));
        return adminEnquirySummaryMapper.toAdminEnquiryDto(adminEnquirySummary);
    }

    @Transactional
    public void handleEnquiry(Integer enquiryId) {
        changeEnquiryStatus(enquiryId, EnquiryStatus.HANDLED);
    }

    @Transactional
    public void reopenEnquiry(Integer enquiryId) {
        changeEnquiryStatus(enquiryId, EnquiryStatus.NEW);
    }

    // Sama staatuse korral midagi ei muutu (updated_at jääb)
    private void changeEnquiryStatus(Integer enquiryId, EnquiryStatus enquiryStatus) {
        Enquiry enquiry = getValidEnquiryBy(enquiryId);
        if (enquiryStatus.getCode().equals(enquiry.getStatus())) {
            return;
        }
        enquiry.setStatus(enquiryStatus.getCode());
        enquiryRepository.save(enquiry);
    }
}
