package ee.bcskoolitus.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Väli peab olema korrektne Base64 (Base64.getDecoder()). null väärtust ei kontrollita.
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidBase64Validator.class)
public @interface ValidBase64 {

    String message() default "peab olema korrektne Base64";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
