package luyen.tradebot.Trade.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GenderSubSetValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenderSubSet {
    Gender[] anyOf() default {};
    String message() default "Invalid Gender";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
