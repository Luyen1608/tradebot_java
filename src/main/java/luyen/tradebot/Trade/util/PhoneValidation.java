package luyen.tradebot.Trade.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PhoneValidation implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public void initialize(PhoneNumber phoneNumberNo) {
    }

    @Override
    public boolean isValid(String phoneNo, ConstraintValidatorContext cxt) {
        if (phoneNo == null) {
            return false;
        }
        //validation phone number of format "0904523452"
        if (phoneNo.matches("\\d{10}")) return true;
            //validation phone number with - , . or space : 090-234-4235
        else if (phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
            //validation phone number with - , . or space : 090-234-4235
        else {
            if (phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
            else return phoneNo.matches("\\d{3}-\\d{3}-\\d{4}");
        }
    }
}
