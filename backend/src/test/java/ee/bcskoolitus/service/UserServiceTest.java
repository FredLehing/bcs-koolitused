package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.SignupRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.participant.Participant;
import ee.bcskoolitus.persistance.participant.ParticipantRepository;
import ee.bcskoolitus.persistance.profile.Profile;
import ee.bcskoolitus.persistance.profile.ProfileMapper;
import ee.bcskoolitus.persistance.profile.ProfileMapperImpl;
import ee.bcskoolitus.persistance.role.Role;
import ee.bcskoolitus.persistance.role.RoleRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.user.UserMapper;
import ee.bcskoolitus.persistance.user.UserMapperImpl;
import ee.bcskoolitus.persistance.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private ParticipantService participantService;
    @Spy
    private UserMapper userMapper = new UserMapperImpl();
    @Spy
    private ProfileMapper profileMapper = new ProfileMapperImpl();

    @InjectMocks
    private UserService userService;

    // ---------- konto loomine ----------

    @Test
    void addUser_createsActiveParticipantUserWithProfile() {
        when(userRepository.existsByEmailIgnoreCase("kati.karu@example.com")).thenReturn(false);
        when(roleRepository.findByName("participant")).thenReturn(Optional.of(createRole()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(8);
            return user;
        });

        LoginResponse loginResponse = userService.addUser(createSignupRequestDto(" kati.karu@example.com "));

        assertEquals(8, loginResponse.getUserId());
        assertEquals("participant", loginResponse.getRoleName());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("kati.karu@example.com", userCaptor.getValue().getEmail());
        assertEquals("salasona1", userCaptor.getValue().getPassword());
        assertEquals("A", userCaptor.getValue().getStatus());
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(participantService).addParticipant(eq(userCaptor.getValue()), profileCaptor.capture());
        assertEquals("Kati", profileCaptor.getValue().getFirstName());
        assertEquals("Karu", profileCaptor.getValue().getLastName());
        assertEquals("kati.karu@example.com", profileCaptor.getValue().getEmail());
        assertEquals("+37255512300", profileCaptor.getValue().getPhone());
    }

    @Test
    void addUser_emailTakenThrows() {
        when(userRepository.existsByEmailIgnoreCase("Anna.Saar@Example.com")).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userService.addUser(createSignupRequestDto("Anna.Saar@Example.com")));

        assertEquals("Selle e-posti aadressiga konto on juba olemas", exception.getMessage());
        assertEquals("EMAIL_TAKEN", exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(participantService, never()).addParticipant(any(), any());
    }

    // ---------- osaleja andmed ----------

    @Test
    void getMyParticipant_returnsParticipantProfile() {
        when(userRepository.findById(2)).thenReturn(Optional.of(createUser(2, "kasutaja@vali-it.ee")));
        Profile profile = new Profile();
        profile.setFirstName("Anna");
        profile.setLastName("Saar");
        profile.setEmail("anna.saar@example.com");
        profile.setPhone("+37256789012");
        Participant participant = new Participant();
        participant.setId(1);
        participant.setProfile(profile);
        when(participantRepository.findByUserId(2)).thenReturn(Optional.of(participant));

        MyParticipantDto myParticipantDto = userService.getMyParticipant(2);

        assertEquals(new MyParticipantDto(1, "Anna", "Saar", "anna.saar@example.com", "+37256789012"), myParticipantDto);
    }

    @Test
    void getMyParticipant_withoutParticipantReturnsUserEmail() {
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser(1, "admin@vali-it.ee")));
        when(participantRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertEquals(new MyParticipantDto(null, "", "", "admin@vali-it.ee", ""), userService.getMyParticipant(1));
    }

    @Test
    void getMyParticipant_unknownUserThrows() {
        when(userRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> userService.getMyParticipant(123));

        assertEquals("Ei leidnud primary keyd 'userId' väärtusega: 123", exception.getMessage());
    }

    private static SignupRequestDto createSignupRequestDto(String email) {
        return new SignupRequestDto("Kati", "Karu", email, "+37255512300", "salasona1");
    }

    private static Role createRole() {
        Role role = new Role();
        role.setId(2);
        role.setName("participant");
        return role;
    }

    private static User createUser(Integer userId, String email) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        return user;
    }
}
