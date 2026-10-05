package ee.bcskoolitus.infrastructure.exception;

public class IncorrectInputException extends RuntimeException {
    public IncorrectInputException(String fieldName) {
        super(fieldName + ": vigane väärtus");
    }
}
