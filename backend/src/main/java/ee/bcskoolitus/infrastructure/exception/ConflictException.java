package ee.bcskoolitus.infrastructure.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    private final String errorCode;
    public ConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
