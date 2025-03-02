package luyen.tradebot.Trade.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;


public class GenderSubSetValidator implements ConstraintValidator<GenderSubSet, Gender> {
    private Gender[] genders;

    @Override
    public void initialize(GenderSubSet contraint) {
        this.genders = contraint.anyOf();
    }

    @Override
    public boolean isValid(Gender value, ConstraintValidatorContext cxt) {
        return value == null || Arrays.asList(genders).contains(value);

    }
}
