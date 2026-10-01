package ee.bcskoolitus.controller.feedback.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tagasiside: hinne 1–10 kohustuslik, kommentaar valikuline kuni 255 märki, vähemalt üks vastus
class FeedbackRequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validRequest_commentMayBeNull() {
        assertTrue(validator.validate(request(new FeedbackAnswerDto(1, 1, null))).isEmpty());
        assertTrue(validator.validate(request(new FeedbackAnswerDto(1, 10, "a".repeat(255)))).isEmpty());
    }

    @Test
    void scoreOutOfRangeOrMissing_isInvalid() {
        assertOnlyViolation(request(new FeedbackAnswerDto(1, 0, null)), "answers[0].score");
        assertOnlyViolation(request(new FeedbackAnswerDto(1, 11, null)), "answers[0].score");
        assertOnlyViolation(request(new FeedbackAnswerDto(1, null, null)), "answers[0].score");
    }

    @Test
    void tooLongComment_missingCriteriaOrNoAnswers_isInvalid() {
        assertOnlyViolation(request(new FeedbackAnswerDto(1, 5, "a".repeat(256))), "answers[0].feedbackText");
        assertOnlyViolation(request(new FeedbackAnswerDto(null, 5, null)), "answers[0].feedbackCriteriaId");
        assertOnlyViolation(new FeedbackRequestDto(List.of()), "answers");
    }

    private static FeedbackRequestDto request(FeedbackAnswerDto feedbackAnswerDto) {
        return new FeedbackRequestDto(List.of(feedbackAnswerDto));
    }

    private static void assertOnlyViolation(FeedbackRequestDto feedbackRequestDto, String field) {
        Set<ConstraintViolation<FeedbackRequestDto>> violations = validator.validate(feedbackRequestDto);
        assertEquals(1, violations.size());
        assertEquals(field, violations.iterator().next().getPropertyPath().toString());
    }
}
