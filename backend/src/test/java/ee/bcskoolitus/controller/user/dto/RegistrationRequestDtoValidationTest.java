package ee.bcskoolitus.controller.user.dto;

import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.controller.enquiry.dto.EnquiryCreateRequestDto;
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

// Konto loomise, registreerumise ja päringu vormid: kohustuslikud väljad, e-posti kuju, parooli pikkus
class RegistrationRequestDtoValidationTest {

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
    void signupRequest_validHasNoViolations() {
        assertTrue(validator.validate(new SignupRequestDto("Kati", "Karu", "kati.karu@example.com", "+37255512300", "salasona1")).isEmpty());
    }

    @Test
    void signupRequest_shortPasswordOrInvalidEmail_isInvalid() {
        assertOnlyViolation(new SignupRequestDto("Kati", "Karu", "kati.karu@example.com", "+37255512300", "lyhike"), "password");
        assertOnlyViolation(new SignupRequestDto("Kati", "Karu", "kati.karu", "+37255512300", "salasona1"), "email");
        assertOnlyViolation(new SignupRequestDto(" ", "Karu", "kati.karu@example.com", "+37255512300", "salasona1"), "firstName");
    }

    @Test
    void courseRegistrationRequest_optionalFieldsMayBeNull() {
        assertTrue(validator.validate(new CourseRegistrationRequestDto(2, "Anna", "Saar", "anna.saar@example.com", "+37256789012", null, null)).isEmpty());
    }

    @Test
    void courseRegistrationRequest_missingUserIdOrPhone_isInvalid() {
        assertOnlyViolation(new CourseRegistrationRequestDto(null, "Anna", "Saar", "anna.saar@example.com", "+37256789012", true, ""), "userId");
        assertOnlyViolation(new CourseRegistrationRequestDto(2, "Anna", "Saar", "anna.saar@example.com", "", true, ""), "phone");
    }

    @Test
    void enquiryCreateRequest_courseAndCompanyAreOptional() {
        assertTrue(validator.validate(createEnquiryCreateRequestDto("Kas kursusele saab tulla?")).isEmpty());
    }

    @Test
    void enquiryCreateRequest_missingOrTooLongMessage_isInvalid() {
        assertOnlyViolation(createEnquiryCreateRequestDto(""), "message");
        assertOnlyViolation(createEnquiryCreateRequestDto("x".repeat(256)), "message");
    }

    private static EnquiryCreateRequestDto createEnquiryCreateRequestDto(String message) {
        return new EnquiryCreateRequestDto(1, null, "Kati", "Karu", "kati.karu@example.com", "+37255512300", null, message);
    }

    private static <T> void assertOnlyViolation(T requestDto, String propertyPath) {
        Set<ConstraintViolation<T>> violations = validator.validate(requestDto);
        assertEquals(1, violations.size(), violations.toString());
        assertEquals(propertyPath, violations.iterator().next().getPropertyPath().toString());
    }
}
