package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.room.RoomMapper;
import ee.bcskoolitus.persistance.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomDto> findRooms() {
        return roomMapper.toRoomDtos(roomRepository.findAllByOrderByNameAsc());
    }

    public Room getValidRoomBy(Integer roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("roomId", roomId));
    }
}
