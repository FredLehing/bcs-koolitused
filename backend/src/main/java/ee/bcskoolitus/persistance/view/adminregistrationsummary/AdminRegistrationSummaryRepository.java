package ee.bcskoolitus.persistance.view.adminregistrationsummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRegistrationSummaryRepository extends JpaRepository<AdminRegistrationSummary, Long> {

    // Admini nimekiri: kustutatud toimumiskorrad ja koolitused välja; includeCancelled=false → ainult registreerunud,
    // includePast=false → ainult toimumiskorrad, mis pole lõppenud
    @Query("""
            select a from AdminRegistrationSummary a
            where a.contentLanguageCode = :contentLang
              and a.courseStatus <> :deletedStatus
              and a.trainingStatus <> :deletedStatus
              and (:includeCancelled = true or a.status = :registeredStatus)
              and (:includePast = true or a.isPast = false)
            order by a.createdAt desc, a.courseParticipantId desc""")
    List<AdminRegistrationSummary> findAdminRegistrationSummariesBy(String contentLang, boolean includeCancelled, boolean includePast,
                                                                    String registeredStatus, String deletedStatus);

    Optional<AdminRegistrationSummary> findByCourseParticipantIdAndContentLanguageCode(Integer courseParticipantId, String contentLang);
}
