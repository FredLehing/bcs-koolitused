package ee.bcskoolitus.controller.lecturer.dto;

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

// photo: korrektne Base64 või null; isPhotoRemoved ja uus photo korraga → 400; tõlke väljad valideeritakse (@Valid)
class LecturerRequestDtoValidationTest {

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
    void createRequest_validWithAndWithoutPhoto() {
        assertTrue(validator.validate(createLecturerCreateRequestDto(null)).isEmpty());
        assertTrue(validator.validate(createLecturerCreateRequestDto("cGhvdG8=")).isEmpty());
    }

    @Test
    void createRequest_invalidBase64IsInvalid() {
        Set<ConstraintViolation<LecturerCreateRequestDto>> violations = validator.validate(createLecturerCreateRequestDto("not base64!"));

        assertEquals(1, violations.size());
        assertEquals("photo", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void updateRequest_photoRemovedTogetherWithNewPhotoIsInvalid() {
        LecturerUpdateRequestDto lecturerUpdateRequestDto = createLecturerUpdateRequestDto("cGhvdG8=", true);

        Set<ConstraintViolation<LecturerUpdateRequestDto>> violations = validator.validate(lecturerUpdateRequestDto);

        assertEquals(1, violations.size());
        assertEquals("photoRemovedWithoutNewPhoto", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void updateRequest_photoRemovedOrNewPhotoIsValid() {
        assertTrue(validator.validate(createLecturerUpdateRequestDto(null, true)).isEmpty());
        assertTrue(validator.validate(createLecturerUpdateRequestDto("cGhvdG8=", false)).isEmpty());
        assertTrue(validator.validate(createLecturerUpdateRequestDto(null, null)).isEmpty());
    }

    @Test
    void updateRequest_emptyTranslationDescriptionIsInvalid() {
        LecturerUpdateRequestDto lecturerUpdateRequestDto = createLecturerUpdateRequestDto(null, false);
        lecturerUpdateRequestDto.getLecturerTranslation().setDescription("<p></p>");

        Set<ConstraintViolation<LecturerUpdateRequestDto>> violations = validator.validate(lecturerUpdateRequestDto);

        assertEquals(1, violations.size());
        assertEquals("lecturerTranslation.description", violations.iterator().next().getPropertyPath().toString());
    }

    private static LecturerCreateRequestDto createLecturerCreateRequestDto(String photo) {
        return new LecturerCreateRequestDto(1, "Kristjan Kuusk", photo, "image/png", "Andmeinsener", "SQL.", "<p>SQL</p>");
    }

    private static LecturerUpdateRequestDto createLecturerUpdateRequestDto(String photo, Boolean isPhotoRemoved) {
        return new LecturerUpdateRequestDto("Rain Tüür", photo, "image/png", isPhotoRemoved,
                new LecturerTranslationUpdateDto(1, "Lektor", "Java.", "<p>Java</p>"));
    }
}
