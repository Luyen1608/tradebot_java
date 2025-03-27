package luyen.tradebot.Trade.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_at", length = 255)
//    @Temporal(TemporalType.DATE)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(name = "updated_at", length = 255)
//    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    private LocalDateTime updateAt;
}
