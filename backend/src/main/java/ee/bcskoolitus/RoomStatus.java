package ee.bcskoolitus;

import lombok.Getter;

// Koolitusruumi staatus (room.status). Kustutatud ruum (soft delete) on teenustes nagu olematu.
@Getter
public enum RoomStatus {
    ACTIVE("A"),
    DELETED("D");

    private final String code;

    RoomStatus(String code) {
        this.code = code;
    }
}
