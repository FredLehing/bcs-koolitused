package ee.bcskoolitus.persistance.view.adminroomsummary;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

// View admin_room_summary: üks rida ruumi kohta. Sisaldab ka kustutatud ruume (status D).
@Getter
@Entity
@Immutable
@Table(name = "admin_room_summary", schema = "bcs_koolitused")
public class AdminRoomSummary {
    @Id
    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    // Toimumiskorrad selles ruumis: end_date >= täna, status NOT IN ('D', 'X')
    @Column(name = "upcoming_course_count", nullable = false)
    private Long upcomingCourseCount;

    // Kõik toimumiskorrad selles ruumis, status <> 'D'
    @Column(name = "course_count", nullable = false)
    private Long courseCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
