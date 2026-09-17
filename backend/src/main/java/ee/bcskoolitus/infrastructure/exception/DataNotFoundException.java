package ee.bcskoolitus.infrastructure.exception;

import lombok.Getter;

@Getter
public class DataNotFoundException extends RuntimeException {
    private final String message;
    private final String errorCode;

    public DataNotFoundException(String message, String errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }
}
