package ee.bcskoolitus.controller.training.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// curriculum (Base64), isCurriculumRemoved ja curriculumLabel → 400 INCORRECT_INPUT
class TrainingCurriculumValidationTest {

    private static final String PDF_BASE64 = "JVBERi0xLjcK";

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
    void updateRequest_validCurriculumFields_isValid() {
        assertTrue(validator.validate(createUpdateRequest(PDF_BASE64, false, "Õppekava")).isEmpty());
        assertTrue(validator.validate(createUpdateRequest(null, true, "Õppekava")).isEmpty());
        assertTrue(validator.validate(createUpdateRequest(null, null, "Õppekava")).isEmpty());
    }

    @Test
    void updateRequest_removedTogetherWithNewCurriculum_isInvalid() {
        Set<ConstraintViolation<TrainingUpdateRequestDto>> violations = validator.validate(createUpdateRequest(PDF_BASE64, true, "Õppekava"));

        assertEquals(1, violations.size());
        assertEquals("curriculumRemovedWithoutNewCurriculum", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void updateRequest_invalidBase64_isInvalid() {
        assertSingleViolation(validator.validate(createUpdateRequest("ei ole base64!", false, "Õppekava")), "curriculum");
    }

    @Test
    void updateRequest_missingOrLongCurriculumLabel_isInvalid() {
        assertSingleViolation(validator.validate(createUpdateRequest(null, false, null)), "curriculumLabel");
        assertSingleViolation(validator.validate(createUpdateRequest(null, false, " ")), "curriculumLabel");
        assertSingleViolation(validator.validate(createUpdateRequest(null, false, "a".repeat(51))), "curriculumLabel");
    }

    @Test
    void createRequest_missingCurriculumLabel_isInvalid() {
        TrainingCreateRequestDto trainingCreateRequestDto = new TrainingCreateRequestDto(1, 1, 1, 1, List.of(), true, false,
                "Java algkursus", "Java alused.", "<p>Java alused.</p>", List.of(), PDF_BASE64, null);

        assertSingleViolation(validator.validate(trainingCreateRequestDto), "curriculumLabel");
    }

    @Test
    void translationCreateRequest_invalidBase64AndMissingLabel_isInvalid() {
        TrainingTranslationCreateRequestDto trainingTranslationCreateRequestDto = new TrainingTranslationCreateRequestDto(
                2, "Java Basics", "Java basics.", "<p>Java basics.</p>", "ei ole base64!", "");

        Set<ConstraintViolation<TrainingTranslationCreateRequestDto>> violations = validator.validate(trainingTranslationCreateRequestDto);

        assertEquals(Set.of("curriculum", "curriculumLabel"),
                violations.stream().map(violation -> violation.getPropertyPath().toString()).collect(Collectors.toSet()));
    }

    private static TrainingUpdateRequestDto createUpdateRequest(String curriculum, Boolean isCurriculumRemoved, String curriculumLabel) {
        return new TrainingUpdateRequestDto(1, 1, 1, List.of(), true, false, List.of(), 1,
                "Java algkursus", "Java alused.", "<p>Java alused.</p>", curriculum, isCurriculumRemoved, curriculumLabel);
    }

    private static <T> void assertSingleViolation(Set<ConstraintViolation<T>> violations, String propertyPath) {
        assertEquals(1, violations.size());
        assertEquals(propertyPath, violations.iterator().next().getPropertyPath().toString());
    }
}
