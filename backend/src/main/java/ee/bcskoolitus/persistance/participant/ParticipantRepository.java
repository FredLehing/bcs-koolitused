package ee.bcskoolitus.persistance.participant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    // Kasutajal on üks oma osaleja (participant_user_uq)
    Optional<Participant> findByUserId(Integer userId);
}
