package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.room.RoomMapper;
import ee.bcskoolitus.persistance.room.RoomMapperImpl;
import ee.bcskoolitus.persistance.room.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Spy
    private RoomMapper roomMapper = new RoomMapperImpl();

    @InjectMocks
    private RoomService roomService;

    @Test
    void findRooms_returnsRoomsInRepositoryOrder() {
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(List.of(createRoom(1, "Assauwe", "VAB"), createRoom(2, "Bremeni", "KIN")));

        assertEquals(List.of(new RoomDto(1, "Assauwe", "VAB"), new RoomDto(2, "Bremeni", "KIN")), roomService.findRooms());
    }

    @Test
    void getValidRoomBy_unknownRoomThrows() {
        when(roomRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class, () -> roomService.getValidRoomBy(123));

        assertEquals("Ei leidnud primary keyd 'roomId' väärtusega: 123", exception.getMessage());
    }

    private static Room createRoom(Integer roomId, String name, String status) {
        Room room = new Room();
        room.setId(roomId);
        room.setName(name);
        room.setStatus(status);
        return room;
    }
}
