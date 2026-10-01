package ee.bcskoolitus;

import lombok.Getter;

// Toimumiskorra osaleja staatus (course_participant.status)
@Getter
public enum CourseParticipantStatus {
    REGISTERED("R"),
    CANCELLED("C");

    private final String code;

    CourseParticipantStatus(String code) {
        this.code = code;
    }
}
