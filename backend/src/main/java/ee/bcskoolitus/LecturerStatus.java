package ee.bcskoolitus;

import lombok.Getter;

// Koolitaja staatus (lecturer.status). Kustutatud koolitaja (soft delete) on teenustes nagu olematu.
@Getter
public enum LecturerStatus {
    ACTIVE("A"),
    DELETED("D");

    private final String code;

    LecturerStatus(String code) {
        this.code = code;
    }
}
