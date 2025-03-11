package luyen.tradebot.Trade.model;


import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_address")
@Entity(name = "Address")
public class AddressEntity  extends AbstractEntity{

    @Column(name = "apartment_number", length = 255)
    private String apartmentNumber;
    @Column(name = "floor", length = 255)
    private String floor;

    @Column(name = "building", length = 255)
    private String building;

    @Column(name = "street_number", length = 255)
    private String streetNumber;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "country", length = 255)
    private String country;

    @Column(name = "address_type", length = 255)
    private Integer addressType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
