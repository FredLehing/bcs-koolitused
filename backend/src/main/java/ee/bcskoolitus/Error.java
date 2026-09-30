package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale email või parool"),
    TRANSLATION_EXISTS("Selles keeles tõlge on juba olemas");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}
