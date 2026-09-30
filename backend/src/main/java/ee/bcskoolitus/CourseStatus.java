package ee.bcskoolitus;

import lombok.Getter;

// Toimumiskorra staatus (course.status). "Toimunud" tuleneb kuupäevast (end_date < täna), mitte staatusest.
@Getter
public enum CourseStatus {
    UNPUBLISHED("U"),
    OPEN("O"),
    FULL("F"),
    CANCELLED("X"),
    DELETED("D");

    private final String code;

    CourseStatus(String code) {
        this.code = code;
    }
}
