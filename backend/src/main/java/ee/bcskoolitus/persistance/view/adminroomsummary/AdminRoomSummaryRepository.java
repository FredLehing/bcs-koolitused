package ee.bcskoolitus.persistance.view.adminroomsummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRoomSummaryRepository extends JpaRepository<AdminRoomSummary, Integer> {

    List<AdminRoomSummary> findAllByOrderByNameAscRoomIdAsc();

    List<AdminRoomSummary> findAllByStatusOrderByNameAscRoomIdAsc(String status);
}
