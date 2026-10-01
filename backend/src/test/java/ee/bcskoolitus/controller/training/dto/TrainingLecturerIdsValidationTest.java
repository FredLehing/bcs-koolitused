package ee.bcskoolitus.controller.training.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// lecturerIds: kohustuslik, võib olla tühi, korduvad ja null ID-d → 400 INCORRECT_INPUT
class TrainingLecturerIdsValidationTest {

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
    void createRequest_emptyOrUniqueLecturerIds_isValid() {
        assertTrue(validateCreateRequestLecturerIds(List.of()).isEmpty());
        assertTrue(validateCreateRequestLecturerIds(List.of(1, 8)).isEmpty());
    }

    @Test
    void createRequest_duplicateLecturerIds_isInvalid() {
        Set<ConstraintViolation<TrainingCreateRequestDto>> violations = validateCreateRequestLecturerIds(List.of(1, 8, 1));

        assertEquals(1, violations.size());
        assertEquals("lecturerIds", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void createRequest_missingOrNullLecturerId_isInvalid() {
        assertEquals(1, validateCreateRequestLecturerIds(null).size());
        assertEquals(1, validateCreateRequestLecturerIds(Arrays.asList(1, null)).size());
    }

    @Test
    void updateRequest_duplicateLecturerIds_isInvalid() {
        TrainingUpdateRequestDto trainingUpdateRequestDto = new TrainingUpdateRequestDto(1, 1, 1, List.of(2, 2), true, false,
                List.of(), 1, "Java algkursus", "Java alused.", "<p>Java alused.</p>", null, false, "Õppekava");

        Set<ConstraintViolation<TrainingUpdateRequestDto>> violations = validator.validate(trainingUpdateRequestDto);

        assertEquals(1, violations.size());
        assertEquals("lecturerIds", violations.iterator().next().getPropertyPath().toString());
    }

    private static Set<ConstraintViolation<TrainingCreateRequestDto>> validateCreateRequestLecturerIds(List<Integer> lecturerIds) {
        TrainingCreateRequestDto trainingCreateRequestDto = new TrainingCreateRequestDto(1, 1, 1, 1, lecturerIds, true, false,
                "Java algkursus", "Java alused.", "<p>Java alused.</p>", List.of(), null, "Õppekava");
        return validator.validate(trainingCreateRequestDto);
    }
}
