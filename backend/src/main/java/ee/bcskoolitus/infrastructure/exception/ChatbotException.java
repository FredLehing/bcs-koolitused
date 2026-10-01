package ee.bcskoolitus.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatbotException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public ChatbotException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
