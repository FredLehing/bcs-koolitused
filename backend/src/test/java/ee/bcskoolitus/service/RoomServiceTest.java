package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.room.dto.RoomCreateRequestDto;
import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.controller.room.dto.RoomUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.room.RoomMapper;
import ee.bcskoolitus.persistance.room.RoomMapperImpl;
import ee.bcskoolitus.persistance.room.RoomRepository;
import ee.bcskoolitus.persistance.user.User;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummaryMapper;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummaryMapperImpl;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AdminRoomSummaryRepository adminRoomSummaryRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserService userService;
    @Spy
    private RoomMapper roomMapper = new RoomMapperImpl();
    @Spy
    private AdminRoomSummaryMapper adminRoomSummaryMapper = new AdminRoomSummaryMapperImpl();

    @InjectMocks
    private RoomService roomService;

    @Test
    void findRooms_returnsOnlyActiveRooms() {
        when(roomRepository.findAllByStatusOrderByNameAscIdAsc("A")).thenReturn(List.of(createRoom(1, "Assauwe", "A"), createRoom(3, "Eppingi", "A")));

        assertEquals(List.of(new RoomDto(1, "Assauwe"), new RoomDto(3, "Eppingi")), roomService.findRooms());
    }

    @Test
    void getValidRoomBy_unknownRoomThrows() {
        when(roomRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.getValidRoomBy(123));

        assertEquals("Ei leidnud primary keyd 'roomId' väärtusega: 123", exception.getMessage());
    }

    @Test
    void getValidActiveRoomBy_deletedRoomThrows() {
        when(roomRepository.findById(2)).thenReturn(Optional.of(createRoom(2, "Bremeni", "D")));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.getValidActiveRoomBy(2));

        assertEquals("Ei leidnud primary keyd 'roomId' väärtusega: 2", exception.getMessage());
    }

    @Test
    void getValidAssignableRoomBy_linkedDeletedRoomIsAllowed() {
        Room room = createRoom(2, "Bremeni", "D");
        when(roomRepository.findById(2)).thenReturn(Optional.of(room));

        assertSame(room, roomService.getValidAssignableRoomBy(2, 2));
    }

    @Test
    void getValidAssignableRoomBy_newDeletedRoomThrows() {
        when(roomRepository.findById(2)).thenReturn(Optional.of(createRoom(2, "Bremeni", "D")));

        assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.getValidAssignableRoomBy(2, 1));
        assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.getValidAssignableRoomBy(2, null));
    }

    @Test
    void findAdminRooms_defaultReturnsOnlyActive() {
        roomService.findAdminRooms(false);

        verify(adminRoomSummaryRepository).findAllByStatusOrderByNameAscRoomIdAsc("A");
        verify(adminRoomSummaryRepository, never()).findAllByOrderByNameAscRoomIdAsc();
    }

    @Test
    void findAdminRooms_includeDeletedReturnsAll() {
        roomService.findAdminRooms(true);

        verify(adminRoomSummaryRepository).findAllByOrderByNameAscRoomIdAsc();
    }

    @Test
    void getRoom_returnsActiveRoom() {
        when(roomRepository.findById(4)).thenReturn(Optional.of(createRoom(4, "Hellemanni", "A")));

        assertEquals(new RoomDto(4, "Hellemanni"), roomService.getRoom(4));
    }

    @Test
    void addRoom_savesActiveRoomWithTrimmedName() {
        User user = new User();
        when(userService.getValidUserBy(1)).thenReturn(user);

        roomService.addRoom(new RoomCreateRequestDto(1, "  Tallinna saal "));

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals("Tallinna saal", roomCaptor.getValue().getName());
        assertEquals("A", roomCaptor.getValue().getStatus());
        assertSame(user, roomCaptor.getValue().getCreatedBy());
    }

    @Test
    void addRoom_existingNameThrows() {
        when(roomRepository.existsByNameIgnoreCase("eppingi")).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> roomService.addRoom(new RoomCreateRequestDto(1, "eppingi")));

        assertEquals("ROOM_NAME_EXISTS", exception.getErrorCode());
        assertEquals("Sellise nimega ruum on juba olemas", exception.getMessage());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void updateRoom_changesName() {
        Room room = createRoom(4, "Hellemanni", "A");
        when(roomRepository.findById(4)).thenReturn(Optional.of(room));

        roomService.updateRoom(4, new RoomUpdateRequestDto("Hellemanni saal "));

        assertEquals("Hellemanni saal", room.getName());
        verify(roomRepository).existsByNameIgnoreCaseAndIdNot("Hellemanni saal", 4);
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_otherRoomNameThrows() {
        when(roomRepository.findById(4)).thenReturn(Optional.of(createRoom(4, "Hellemanni", "A")));
        when(roomRepository.existsByNameIgnoreCaseAndIdNot("Eppingi", 4)).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> roomService.updateRoom(4, new RoomUpdateRequestDto("Eppingi")));

        assertEquals("ROOM_NAME_EXISTS", exception.getErrorCode());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void updateRoom_deletedRoomThrows() {
        when(roomRepository.findById(2)).thenReturn(Optional.of(createRoom(2, "Bremeni", "D")));

        assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.updateRoom(2, new RoomUpdateRequestDto("Uus")));
        verify(roomRepository, never()).save(any());
    }

    @Test
    void deleteRoom_setsDeletedStatus() {
        Room room = createRoom(6, "Megede", "A");
        when(roomRepository.findById(6)).thenReturn(Optional.of(room));

        roomService.deleteRoom(6);

        assertEquals("D", room.getStatus());
        verify(courseRepository).countUpcomingCoursesBy(eq(6), eq(LocalDate.now()), eq(List.of("X", "D")));
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_upcomingCoursesThrows() {
        Room room = createRoom(1, "Assauwe", "A");
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(courseRepository.countUpcomingCoursesBy(eq(1), any(), any())).thenReturn(2L);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> roomService.deleteRoom(1));

        assertEquals("ROOM_HAS_UPCOMING_COURSES", exception.getErrorCode());
        assertEquals("Ruumis on tulevasi toimumiskordi, vali neile enne teine ruum", exception.getMessage());
        assertEquals("A", room.getStatus());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void deleteRoom_alreadyDeletedChangesNothing() {
        when(roomRepository.findById(2)).thenReturn(Optional.of(createRoom(2, "Bremeni", "D")));

        roomService.deleteRoom(2);

        verify(roomRepository, never()).save(any());
    }

    @Test
    void restoreRoom_setsActiveStatus() {
        Room room = createRoom(2, "Bremeni", "D");
        when(roomRepository.findById(2)).thenReturn(Optional.of(room));

        roomService.restoreRoom(2);

        assertEquals("A", room.getStatus());
        verify(roomRepository).save(room);
    }

    @Test
    void restoreRoom_activeRoomChangesNothing() {
        when(roomRepository.findById(1)).thenReturn(Optional.of(createRoom(1, "Assauwe", "A")));

        roomService.restoreRoom(1);

        verify(roomRepository, never()).save(any());
    }

    private static Room createRoom(Integer roomId, String name, String status) {
        Room room = new Room();
        room.setId(roomId);
        room.setName(name);
        room.setStatus(status);
        return room;
    }
}
