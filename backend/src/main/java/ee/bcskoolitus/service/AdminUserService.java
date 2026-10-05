package ee.bcskoolitus.service;

import ee.bcskoolitus.ApiStatus;
import ee.bcskoolitus.CourseParticipantStatus;
import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.controller.user.dto.AdminUserDto;
import ee.bcskoolitus.controller.user.dto.AdminUserRegistrationDto;
import ee.bcskoolitus.controller.user.dto.AdminUserSummaryDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.course.participant.CourseParticipant;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantMapper;
import ee.bcskoolitus.persistance.course.participant.CourseParticipantRepository;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.user.UserMapper;
import ee.bcskoolitus.persistance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ee.bcskoolitus.Error.CANNOT_DEACTIVATE_SELF;

// Admini kontode haldus: nimekiri, üks konto, deaktiveerimine (soft delete) ja taastamine
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;
    private final ParticipantRepository participantRepository;
    private final CourseParticipantRepository courseParticipantRepository;
    private final CourseParticipantMapper courseParticipantMapper;
    private final TrainingTranslationService trainingTranslationService;

    // Uusimad eespool; vaikimisi ainult aktiivsed, includeDeleted=true → ka deaktiveeritud
    @Transactional(readOnly = true)
    public List<AdminUserSummaryDto> findAdminUsers(Boolean includeDeleted) {
        return userRepository.findAdminUserSummariesBy(Boolean.TRUE.equals(includeDeleted),
                ApiStatus.STATUS_ACTIVE.getCode(), CourseParticipantStatus.REGISTERED.getCode());
    }

    // Üks konto (ka deaktiveeritud) koos osaleja ja registreerumistega (uusimad toimumiskorrad eespool)
    @Transactional(readOnly = true)
    public AdminUserDto getAdminUser(Integer userId, String contentLang) {
        User user = userService.getValidUserBy(userId);
        AdminUserDto adminUserDto = userMapper.toAdminUserDto(user);
        adminUserDto.setRegistrations(List.of());
        handleAddParticipantData(adminUserDto, userId, contentLang);
        return adminUserDto;
    }

    // Soft delete: status = D; osaleja ja registreerumised jäävad. Iseennast deaktiveerida ei saa.
    @Transactional
    public void deactivateUser(Integer userId, Integer currentUserId) {
        User user = userService.getValidUserBy(userId);
        if (userId.equals(currentUserId)) {
            throw new ForbiddenException(CANNOT_DEACTIVATE_SELF.getMessage(), CANNOT_DEACTIVATE_SELF.name());
        }
        user.setStatus(ApiStatus.STATUS_DELETED.getCode());
        userRepository.save(user);
    }

    // Aktiivse konto korral midagi ei muutu
    @Transactional
    public void restoreUser(Integer userId) {
        User user = userService.getValidUserBy(userId);
        user.setStatus(ApiStatus.STATUS_ACTIVE.getCode());
        userRepository.save(user);
    }

    // Osaleja puudumisel jäävad osaleja väljad null ja registreerumised tühjaks
    private void handleAddParticipantData(AdminUserDto adminUserDto, Integer userId, String contentLang) {
        Optional<Participant> participant = participantRepository.findByUserId(userId);
        if (participant.isEmpty()) {
            return;
        }
        adminUserDto.setParticipantId(participant.get().getId());
        adminUserDto.setParticipantName(participant.get().getName());
        adminUserDto.setProfileEmail(participant.get().getProfile().getEmail());
        adminUserDto.setPhone(participant.get().getProfile().getPhone());
        List<CourseParticipant> courseParticipants = courseParticipantRepository.findParticipantCourseParticipantsBy(
                participant.get().getId(), CourseStatus.DELETED.getCode());
        adminUserDto.setRegistrations(courseParticipants.reversed().stream()
                .map(courseParticipant -> createAdminUserRegistrationDto(courseParticipant, contentLang))
                .toList());
    }

    private AdminUserRegistrationDto createAdminUserRegistrationDto(CourseParticipant courseParticipant, String contentLang) {
        AdminUserRegistrationDto adminUserRegistrationDto = courseParticipantMapper.toAdminUserRegistrationDto(courseParticipant);
        adminUserRegistrationDto.setTrainingTitle(
                trainingTranslationService.getTrainingTitle(courseParticipant.getCourse().getTraining().getId(), contentLang));
        return adminUserRegistrationDto;
    }
}
