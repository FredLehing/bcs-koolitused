package ee.bcskoolitus.service;

import ee.bcskoolitus.ApiRole;
import ee.bcskoolitus.ApiStatus;
import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.PasswordChangeRequestDto;
import ee.bcskoolitus.controller.user.dto.ProfileUpdateRequestDto;
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
import static ee.bcskoolitus.Error.INCORRECT_PASSWORD;

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

    // "Minu andmed": e-post muutub nii kontol (sisselogimine) kui ka profiilis; osalejata kasutajale luuakse profiil + osaleja
    @Transactional
    public void updateProfile(Integer userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getValidUserBy(userId);
        String email = profileUpdateRequestDto.getEmail().trim();
        validateEmailNotTakenByOtherUser(email, userId);
        user.setEmail(email);
        userRepository.save(user);
        Optional<Participant> participant = participantRepository.findByUserId(userId);
        if (participant.isEmpty()) {
            Profile profile = profileMapper.toProfile(profileUpdateRequestDto);
            profile.setEmail(email);
            participantService.addParticipant(user, profile);
            return;
        }
        Profile profile = participant.get().getProfile();
        profileMapper.updateProfile(profileUpdateRequestDto, profile);
        profile.setEmail(email);
        participantService.updateParticipantName(participant.get());
    }

    // Parool on kontol; praegune parool peab klappima (paroolid on praegu lihttekstina)
    @Transactional
    public void updatePassword(Integer userId, PasswordChangeRequestDto passwordChangeRequestDto) {
        User user = getValidUserBy(userId);
        if (!user.getPassword().equals(passwordChangeRequestDto.getCurrentPassword())) {
            throw new ForbiddenException(INCORRECT_PASSWORD.getMessage(), INCORRECT_PASSWORD.name());
        }
        user.setPassword(passwordChangeRequestDto.getNewPassword());
        userRepository.save(user);
    }

    private void validateEmailNotTakenByOtherUser(String email, Integer userId) {
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new ForbiddenException(EMAIL_TAKEN.getMessage(), EMAIL_TAKEN.name());
        }
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
