package ee.bcskoolitus.persistance.user;

import ee.bcskoolitus.controller.user.dto.AdminUserSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u where u.email = :email and u.password = :password and u.status = :status")
    Optional<User> findUserBy(String email, String password, String status);

    // E-post on unikaalne tõstutundetult
    boolean existsByEmailIgnoreCase(String email);

    // E-posti muutmine: kas e-post on mõnel teisel kontol
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    // Admini kontode nimekiri: osaleja ja profiil LEFT JOIN-iga (adminikontodel osalejat tavaliselt pole),
    // registrationCount = registreerunud (R) read; includeDeleted=false → ainult aktiivsed. Uusimad eespool.
    @Query("""
            select new ee.bcskoolitus.controller.user.dto.AdminUserSummaryDto(
                u.id, u.email, r.name, p.name, pr.phone,
                (select count(cp) from CourseParticipant cp where cp.participant = p and cp.status = :registeredStatus),
                u.createdAt, u.status)
            from User u
            join u.role r
            left join Participant p on p.user = u
            left join p.profile pr
            where (:includeDeleted = true or u.status = :activeStatus)
            order by u.createdAt desc, u.id desc""")
    List<AdminUserSummaryDto> findAdminUserSummariesBy(boolean includeDeleted, String activeStatus, String registeredStatus);

}