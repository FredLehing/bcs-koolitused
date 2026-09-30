package ee.bcskoolitus.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// HTML väli peab sisaldama teksti ka pärast puhastamist (HtmlSanitizer) — "<p></p>" ei ole lubatud.
// null väärtust ei kontrollita, selleks on @NotBlank.
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HtmlNotBlankValidator.class)
public @interface HtmlNotBlank {

    String message() default "ei tohi olla tühi";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
