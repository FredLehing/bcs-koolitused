package ee.bcskoolitus.persistance.enquiry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquiryRepository extends JpaRepository<Enquiry, Integer> {
}
