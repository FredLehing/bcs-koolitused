package ee.bcskoolitus.controller.course.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Kohustuslikud väljad, ≥ 1 / ≥ 0, status U/O/F/X (D → 400), korduvad lecturerIds → 400
class CourseRequestDtoValidationTest {

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
    void validRequest_hasNoViolations() {
        for (String status : List.of("U", "O", "F", "X")) {
            assertTrue(validator.validate(createCourseUpdateRequestDto(status, 5, new BigDecimal("0"), List.of(1, 8))).isEmpty(), status);
        }
    }

    @Test
    void deletedStatus_isInvalid() {
        assertOnlyViolation(createCourseUpdateRequestDto("D", 5, new BigDecimal("490"), List.of()), "status");
    }

    @Test
    void zeroDaysOrNegativePrice_isInvalid() {
        assertOnlyViolation(createCourseUpdateRequestDto("O", 0, new BigDecimal("490"), List.of()), "numberOfDays");
        assertOnlyViolation(createCourseUpdateRequestDto("O", 5, new BigDecimal("-1"), List.of()), "price");
    }

    @Test
    void duplicateLecturerIds_isInvalid() {
        assertOnlyViolation(createCourseUpdateRequestDto("O", 5, new BigDecimal("490"), List.of(1, 1)), "lecturerIds");
    }

    @Test
    void createRequest_missingUserId_isInvalid() {
        CourseCreateRequestDto courseCreateRequestDto = new CourseCreateRequestDto(null, LocalDate.of(2027, 1, 11), LocalDate.of(2027, 1, 15),
                5, 40, new BigDecimal("490"), List.of(), null, "U", false, null, null);

        Set<ConstraintViolation<CourseCreateRequestDto>> violations = validator.validate(courseCreateRequestDto);

        assertEquals(1, violations.size());
        assertEquals("userId", violations.iterator().next().getPropertyPath().toString());
    }

    private static void assertOnlyViolation(CourseUpdateRequestDto courseUpdateRequestDto, String propertyPath) {
        Set<ConstraintViolation<CourseUpdateRequestDto>> violations = validator.validate(courseUpdateRequestDto);
        assertEquals(1, violations.size());
        assertEquals(propertyPath, violations.iterator().next().getPropertyPath().toString());
    }

    private static CourseUpdateRequestDto createCourseUpdateRequestDto(String status, Integer numberOfDays, BigDecimal price, List<Integer> lecturerIds) {
        return new CourseUpdateRequestDto(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 23),
                numberOfDays, 40, price, lecturerIds, 2, status, true, null, null);
    }
}
