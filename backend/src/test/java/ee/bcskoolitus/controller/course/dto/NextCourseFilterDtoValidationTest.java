package ee.bcskoolitus.controller.course.dto;

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

// contentLang kohustuslik, limit 1–20 (vaikimisi 5)
class NextCourseFilterDtoValidationTest {

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
    void defaultLimit_isFive() {
        assertEquals(5, new NextCourseFilterDto().getLimit());
    }

    @Test
    void limitWithinRange_hasNoViolations() {
        for (int limit : new int[]{1, 5, 20}) {
            assertTrue(validator.validate(createNextCourseFilterDto("et", limit)).isEmpty(), String.valueOf(limit));
        }
    }

    @Test
    void limitOutOfRange_isInvalid() {
        assertOnlyViolation(createNextCourseFilterDto("et", 0), "limit");
        assertOnlyViolation(createNextCourseFilterDto("et", 21), "limit");
    }

    @Test
    void missingContentLang_isInvalid() {
        assertOnlyViolation(createNextCourseFilterDto(null, 5), "contentLang");
    }

    private static void assertOnlyViolation(NextCourseFilterDto nextCourseFilterDto, String propertyPath) {
        Set<ConstraintViolation<NextCourseFilterDto>> violations = validator.validate(nextCourseFilterDto);
        assertEquals(1, violations.size());
        assertEquals(propertyPath, violations.iterator().next().getPropertyPath().toString());
    }

    private static NextCourseFilterDto createNextCourseFilterDto(String contentLang, Integer limit) {
        NextCourseFilterDto nextCourseFilterDto = new NextCourseFilterDto();
        nextCourseFilterDto.setContentLang(contentLang);
        nextCourseFilterDto.setLimit(limit);
        return nextCourseFilterDto;
    }
}
