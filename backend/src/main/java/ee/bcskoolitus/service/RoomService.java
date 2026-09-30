package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.RoomStatus;
import ee.bcskoolitus.controller.room.dto.AdminRoomSummaryDto;
import ee.bcskoolitus.controller.room.dto.RoomCreateRequestDto;
import ee.bcskoolitus.controller.room.dto.RoomDto;
import ee.bcskoolitus.controller.room.dto.RoomUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.room.RoomMapper;
import ee.bcskoolitus.persistance.room.RoomRepository;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummary;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummaryMapper;
import ee.bcskoolitus.persistance.view.adminroomsummary.AdminRoomSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static ee.bcskoolitus.Error.ROOM_HAS_UPCOMING_COURSES;
import static ee.bcskoolitus.Error.ROOM_NAME_EXISTS;

@Service
@RequiredArgsConstructor
public class RoomService {

    // Tühistatud ja kustutatud toimumiskorrad ei ole "tulevased" (kustutamist ei takista)
    private static final List<String> NOT_UPCOMING_COURSE_STATUSES =
            List.of(CourseStatus.CANCELLED.getCode(), CourseStatus.DELETED.getCode());

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AdminRoomSummaryRepository adminRoomSummaryRepository;
    private final AdminRoomSummaryMapper adminRoomSummaryMapper;
    private final CourseRepository courseRepository;
    private final UserService userService;

    // Ainult aktiivsed — kustutatud ruumi ei saa toimumiskorrale valida
    public List<RoomDto> findRooms() {
        return roomMapper.toRoomDtos(roomRepository.findAllByStatusOrderByNameAscIdAsc(RoomStatus.ACTIVE.getCode()));
    }

    // Leiab ka kustutatud ruumi — kasutavad delete ja restore
    public Room getValidRoomBy(Integer roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("roomId", roomId));
    }

    // Kustutatud ruum (status D) on nagu olematu → 404
    public Room getValidActiveRoomBy(Integer roomId) {
        Room room = getValidRoomBy(roomId);
        if (RoomStatus.DELETED.getCode().equals(room.getStatus())) {
            throw new PrimaryKeyNotFoundException("roomId", roomId);
        }
        return room;
    }

    // Toimumiskorra ruum: uus peab olema aktiivne, praegune (linkedRoomId) võib olla ka kustutatud,
    // et muid välju saaks edasi salvestada
    public Room getValidAssignableRoomBy(Integer roomId, Integer linkedRoomId) {
        if (roomId.equals(linkedRoomId)) {
            return getValidRoomBy(roomId);
        }
        return getValidActiveRoomBy(roomId);
    }

    public List<AdminRoomSummaryDto> findAdminRooms(Boolean includeDeleted) {
        List<AdminRoomSummary> adminRoomSummaries = Boolean.TRUE.equals(includeDeleted)
                ? adminRoomSummaryRepository.findAllByOrderByNameAscRoomIdAsc()
                : adminRoomSummaryRepository.findAllByStatusOrderByNameAscRoomIdAsc(RoomStatus.ACTIVE.getCode());
        return adminRoomSummaryMapper.toAdminRoomSummaryDtos(adminRoomSummaries);
    }

    public RoomDto getRoom(Integer roomId) {
        return roomMapper.toRoomDto(getValidActiveRoomBy(roomId));
    }

    @Transactional
    public void addRoom(RoomCreateRequestDto roomCreateRequestDto) {
        String roomName = roomCreateRequestDto.getRoomName().trim();
        if (roomRepository.existsByNameIgnoreCase(roomName)) {
            throwRoomNameExists();
        }
        Room room = new Room();
        room.setName(roomName);
        room.setStatus(RoomStatus.ACTIVE.getCode());
        room.setCreatedBy(userService.getValidUserBy(roomCreateRequestDto.getUserId()));
        roomRepository.save(room);
    }

    @Transactional
    public void updateRoom(Integer roomId, RoomUpdateRequestDto roomUpdateRequestDto) {
        Room room = getValidActiveRoomBy(roomId);
        String roomName = roomUpdateRequestDto.getRoomName().trim();
        if (roomRepository.existsByNameIgnoreCaseAndIdNot(roomName, roomId)) {
            throwRoomNameExists();
        }
        room.setName(roomName);
        roomRepository.save(room);
    }

    private static void throwRoomNameExists() {
        throw new ForbiddenException(ROOM_NAME_EXISTS.getMessage(), ROOM_NAME_EXISTS.name());
    }

    // Soft delete: ainult status = D; toimumiskordade seosed jäävad alles. Korduv kustutamine ei muuda midagi.
    @Transactional
    public void deleteRoom(Integer roomId) {
        Room room = getValidRoomBy(roomId);
        if (RoomStatus.DELETED.getCode().equals(room.getStatus())) {
            return;
        }
        validateRoomHasNoUpcomingCourses(roomId);
        room.setStatus(RoomStatus.DELETED.getCode());
        roomRepository.save(room);
    }

    private void validateRoomHasNoUpcomingCourses(Integer roomId) {
        long upcomingCourseCount = courseRepository.countUpcomingCoursesBy(roomId, LocalDate.now(), NOT_UPCOMING_COURSE_STATUSES);
        if (upcomingCourseCount > 0) {
            throw new ForbiddenException(ROOM_HAS_UPCOMING_COURSES.getMessage(), ROOM_HAS_UPCOMING_COURSES.name());
        }
    }

    // Aktiivse ruumi korral midagi ei muutu
    @Transactional
    public void restoreRoom(Integer roomId) {
        Room room = getValidRoomBy(roomId);
        if (RoomStatus.ACTIVE.getCode().equals(room.getStatus())) {
            return;
        }
        room.setStatus(RoomStatus.ACTIVE.getCode());
        roomRepository.save(room);
    }
}
