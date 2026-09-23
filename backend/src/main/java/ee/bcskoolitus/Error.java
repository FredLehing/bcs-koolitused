package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale email või parool");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}
