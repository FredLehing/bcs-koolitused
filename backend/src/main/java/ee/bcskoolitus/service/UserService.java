package ee.bcskoolitus.service;

import ee.bcskoolitus.ApiRole;
import ee.bcskoolitus.ApiStatus;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.SignupRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.role.Role;
import ee.bcskoolitus.persistance.role.RoleRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.user.UserMapper;
import ee.bcskoolitus.persistance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ee.bcskoolitus.Error.EMAIL_TAKEN;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final ParticipantRepository participantRepository;
    private final ProfileMapper profileMapper;
    private final ParticipantService participantService;

    public User getValidUserBy(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    // Konto loomine: kasutaja (roll participant, aktiivne) + profiil + osaleja ühes transaktsioonis; vastus nagu sisselogimisel
    @Transactional
    public LoginResponse addUser(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail().trim();
        validateEmailNotTaken(email);
        User user = new User();
        user.setRole(getParticipantRole());
        user.setEmail(email);
        user.setPassword(signupRequestDto.getPassword());
        user.setStatus(ApiStatus.STATUS_ACTIVE.getCode());
        userRepository.save(user);
        Profile profile = profileMapper.toProfile(signupRequestDto);
        profile.setEmail(email);
        participantService.addParticipant(user, profile);
        return userMapper.toLoginResponse(user);
    }

    // Registreerumise vormi eeltäitmine kasutaja osaleja profiilist
    @Transactional(readOnly = true)
    public MyParticipantDto getMyParticipant(Integer userId) {
        User user = getValidUserBy(userId);
        Optional<Participant> participant = participantRepository.findByUserId(userId);
        if (participant.isEmpty()) {
            return new MyParticipantDto(null, "", "", user.getEmail(), "");
        }
        MyParticipantDto myParticipantDto = profileMapper.toMyParticipantDto(participant.get().getProfile());
        myParticipantDto.setParticipantId(participant.get().getId());
        return myParticipantDto;
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ForbiddenException(EMAIL_TAKEN.getMessage(), EMAIL_TAKEN.name());
        }
    }

    private Role getParticipantRole() {
        return roleRepository.findByName(ApiRole.ROLE_PARTICIPANT.getName())
                .orElseThrow(() -> new IllegalStateException("Roll puudub (role.name = participant)"));
    }
}
