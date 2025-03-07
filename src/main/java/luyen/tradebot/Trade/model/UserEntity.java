package luyen.tradebot.Trade.model;

import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.Gender;
import luyen.tradebot.Trade.util.UserStatus;
import luyen.tradebot.Trade.util.UserType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_user")
public class UserEntity extends AbstractEntity {

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "gender", length = 255)
    private Gender gender;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "user_name", unique = true, nullable = false, length = 255)
    private String userName;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "user_type", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserType userType;

    @Column(name = "user_status", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus userStatus;
}
