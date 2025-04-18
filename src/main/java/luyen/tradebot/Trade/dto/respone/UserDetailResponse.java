package luyen.tradebot.Trade.dto.respone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import luyen.tradebot.Trade.util.PhoneNumber;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserDetailResponse  implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public UserDetailResponse(UUID id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
