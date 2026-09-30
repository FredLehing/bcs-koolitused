package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale email või parool"),
    TRANSLATION_EXISTS("Selles keeles tõlge on juba olemas"),
    TRAINING_DELETED("Kustutatud koolituse staatust ei saa muuta, taasta see enne");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}
