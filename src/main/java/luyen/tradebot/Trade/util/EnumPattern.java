package luyen.tradebot.Trade.util;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,ElementType.PARAMETER,ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumPatternValidator.class)
public @interface EnumPattern {
    String name() default "";
    String regex() default "";
    String message() default "{name} must match {regex}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
