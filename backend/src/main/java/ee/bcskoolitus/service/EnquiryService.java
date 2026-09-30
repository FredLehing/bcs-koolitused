package ee.bcskoolitus.service;

import ee.bcskoolitus.EnquiryStatus;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquirySummaryDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.enquiry.Enquiry;
import ee.bcskoolitus.persistance.enquiry.EnquiryRepository;
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
