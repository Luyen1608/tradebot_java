package luyen.tradebot.Trade.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import luyen.tradebot.Trade.util.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO implements Serializable {

    @NotBlank(message = "First Name not Blank")
    private String firstName;
    @NotNull(message = "Last Name not Null")
    private String lastName;
    @Email(message = "Email invalid Format")
    private String email;
//    @Pattern(regexp = "^\\d{10}$", message = "Phone invalid format")
    @PhoneNumber
    private String phone;

//    @Pattern(regexp = "^ACTIVE|INACTIVE|NONE$", message="Status must be one in {ACTIVE, INACTIVE,NONE")
    @GenderSubSet(anyOf = {Gender.MALE, Gender.FEMALE})
    private Gender gender;

    @EnumPattern(name = "status", regex = "ACTIVE|INACTIVE|NONE")
    private UserStatus status;

    @EnumValue(name = "Type", enumClass = UserType.class)
    private String type;

    @NotNull(message = "Date of birth must be not null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;

//    @NotNull(message = "User Name not Null")
    private String userName;

//    @NotNull(message = "Pass world not Null")
    private String passWord;

    @NotEmpty
    private List<String> permission;

    @NotEmpty
    private Set<AddressRequestDTO> addresses;

    public UserRequestDTO(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
}
