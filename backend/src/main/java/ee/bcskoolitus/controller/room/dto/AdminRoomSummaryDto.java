package ee.bcskoolitus.controller.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRoomSummaryDto {

    private Integer roomId;
    private String roomName;
    private String status;
    private Long upcomingCourseCount;
    private Long courseCount;
    private Instant updatedAt;
}
