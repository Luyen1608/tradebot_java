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



//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//    public Date getDateOfBirth() {
//        return dateOfBirth;
//    }
//
//    public void setDateOfBirth(Date dateOfBirth) {
//        this.dateOfBirth = dateOfBirth;
//    }
//
//    public List<String> getPermission() {
//        return permission;
//    }
//
//    public void setPermission(List<String> permission) {
//        this.permission = permission;
//    }
//
//    public UserStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(UserStatus status) {
//        this.status = status;
//    }
//
//    public Gender getGender() {
//        return gender;
//    }
//
//    public void setGender(Gender gender) {
//        this.gender = gender;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
}
