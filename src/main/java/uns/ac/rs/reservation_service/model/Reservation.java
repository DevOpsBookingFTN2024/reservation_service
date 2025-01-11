package uns.ac.rs.reservation_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "guest")
    private String guest;

    @Column(name = "id_accommodation")
    private UUID idAccommodation;

    @Column(name = "dates")
    private Set<LocalDate> dates;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "is_declined")
    private Boolean isDeclined;

    @Column(name = "is_cancelled")
    private Boolean isCancelled;

    public Reservation(String guest,
                       UUID idAccommodation,
                       Set<LocalDate> dates,
                       Integer numberOfGuests) {
        this.guest = guest;
        this.idAccommodation = idAccommodation;
        this.dates = dates;
        this.numberOfGuests = numberOfGuests;
    }
}
