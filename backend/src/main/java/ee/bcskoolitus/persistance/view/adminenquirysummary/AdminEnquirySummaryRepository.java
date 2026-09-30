package ee.bcskoolitus.persistance.view.adminenquirysummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminEnquirySummaryRepository extends JpaRepository<AdminEnquirySummary, Long> {

    List<AdminEnquirySummary> findAllByContentLanguageCodeOrderByCreatedAtDescEnquiryIdDesc(String contentLang);

    List<AdminEnquirySummary> findAllByContentLanguageCodeAndStatusOrderByCreatedAtDescEnquiryIdDesc(String contentLang, String status);

    Optional<AdminEnquirySummary> findByEnquiryIdAndContentLanguageCode(Integer enquiryId, String contentLang);
}
