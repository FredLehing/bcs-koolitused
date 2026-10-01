package ee.bcskoolitus.service;

import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileRepository;
import ee.bcskoolitus.persistance.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ProfileRepository profileRepository;

    // Kasutaja oma osaleja koos profiiliga; participant.name = eesnimi + perekonnanimi
    public Participant addParticipant(User user, Profile profile) {
        profileRepository.save(profile);
        Participant participant = new Participant();
        participant.setUser(user);
        participant.setProfile(profile);
        participant.setName(createParticipantName(profile));
        return participantRepository.save(participant);
    }

    // Nimi tuletatakse profiilist — kutsu pärast profiili muutmist
    public void updateParticipantName(Participant participant) {
        participant.setName(createParticipantName(participant.getProfile()));
        participantRepository.save(participant);
    }

    private static String createParticipantName(Profile profile) {
        return profile.getFirstName() + " " + profile.getLastName();
    }
}
