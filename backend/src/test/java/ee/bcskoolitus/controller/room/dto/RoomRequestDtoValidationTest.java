package ee.bcskoolitus.controller.room.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// roomName: kohustuslik, mitte ainult tühikud, kuni 255 märki; userId kohustuslik
class RoomRequestDtoValidationTest {

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
    void createRequest_validRequestHasNoViolations() {
        assertTrue(validator.validate(new RoomCreateRequestDto(1, "Tallinna saal")).isEmpty());
    }

    @Test
    void createRequest_blankNameAndMissingUserAreInvalid() {
        Set<ConstraintViolation<RoomCreateRequestDto>> violations = validator.validate(new RoomCreateRequestDto(null, "  "));

        assertEquals(2, violations.size());
    }

    @Test
    void updateRequest_tooLongNameIsInvalid() {
        Set<ConstraintViolation<RoomUpdateRequestDto>> violations = validator.validate(new RoomUpdateRequestDto("a".repeat(256)));

        assertEquals(1, violations.size());
        assertEquals("roomName", violations.iterator().next().getPropertyPath().toString());
    }
}
