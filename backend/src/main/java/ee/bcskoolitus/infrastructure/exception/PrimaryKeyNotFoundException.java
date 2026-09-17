package ee.bcskoolitus.infrastructure.exception;

import lombok.Getter;

@Getter
public class PrimaryKeyNotFoundException extends RuntimeException {
    private final String message;
    private final String errorCode;

    public PrimaryKeyNotFoundException(String fieldName, Integer fieldValue) {
        super("Ei leidnud primary keyd '" + fieldName + "' väärtusega: " + fieldValue);
        this.message = "Ei leidnud primary keyd '" + fieldName + "' väärtusega: " + fieldValue;
        this.errorCode = "PRIMARY_KEY_NOT_FOUND";
    }
}
