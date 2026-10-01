package ee.bcskoolitus.persistance.enquiry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Integer> {

    // Toimumiskorra päringud, uusimad eespool
    List<Enquiry> findAllByCourseIdOrderByCreatedAtDescIdDesc(Integer courseId);
}
