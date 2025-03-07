package luyen.tradebot.Trade.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import luyen.tradebot.Trade.util.Gender;
import luyen.tradebot.Trade.util.UserType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserCreationRequest implements Serializable {
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private Date birthDate;
    private String phone;
    private String userName;
    private UserType userType;
    private List<AddressRequest> address; // home , office
}
