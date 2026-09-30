package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.enquiry.Enquiry;
import ee.bcskoolitus.persistance.enquiry.EnquiryRepository;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryMapper;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryMapperImpl;
import ee.bcskoolitus.persistance.view.adminenquirysummary.AdminEnquirySummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Spy
    private AdminEnquirySummaryMapper adminEnquirySummaryMapper = new AdminEnquirySummaryMapperImpl();

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

    private static Enquiry createEnquiry(Integer enquiryId, String status) {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(enquiryId);
        enquiry.setStatus(status);
        return enquiry;
    }
}
