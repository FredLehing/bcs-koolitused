package ee.bcskoolitus.controller.courseparticipant.dto;

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

// Registreerumise muutmine (admin): staatus R/C, kohustuslikud lülitid, admini märkmed valikulised
class AdminRegistrationUpdateRequestDtoValidationTest {

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
    void validRequest_adminNotesMayBeNull() {
        assertTrue(validator.validate(new AdminRegistrationUpdateRequestDto("R", false, true, null)).isEmpty());
        assertTrue(validator.validate(new AdminRegistrationUpdateRequestDto("C", true, false, "Teatas telefoni teel 25.09.")).isEmpty());
    }

    @Test
    void unknownOrMissingStatus_isInvalid() {
        assertOnlyViolation(new AdminRegistrationUpdateRequestDto("X", false, false, null), "status");
        assertOnlyViolation(new AdminRegistrationUpdateRequestDto("RC", false, false, null), "status");
        assertOnlyViolation(new AdminRegistrationUpdateRequestDto(null, false, false, null), "status");
    }

    @Test
    void missingSwitches_areInvalid() {
        assertOnlyViolation(new AdminRegistrationUpdateRequestDto("R", null, false, null), "hasPaid");
        assertOnlyViolation(new AdminRegistrationUpdateRequestDto("R", false, null, null), "requiresLaptop");
    }

    private static void assertOnlyViolation(AdminRegistrationUpdateRequestDto adminRegistrationUpdateRequestDto, String field) {
        Set<ConstraintViolation<AdminRegistrationUpdateRequestDto>> violations = validator.validate(adminRegistrationUpdateRequestDto);
        assertEquals(1, violations.size());
        assertEquals(field, violations.iterator().next().getPropertyPath().toString());
    }
}
