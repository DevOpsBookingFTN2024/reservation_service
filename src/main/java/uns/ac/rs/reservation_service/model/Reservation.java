package uns.ac.rs.reservation_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
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

    @Column(name = "host")
    private String host;

    @Column(name = "id_accommodation")
    private UUID idAccommodation;

    @Column(name = "accommodation_name")
    private String accommodationName;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "total_price")
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status")
    private EReservationStatus reservationStatus;

    public Reservation(String guest,
                       String host,
                       UUID idAccommodation,
                       String accommodationName,
                       LocalDate dateFrom,
                       LocalDate dateTo,
                       Integer numberOfGuests) {
        this.guest = guest;
        this.host = host;
        this.idAccommodation = idAccommodation;
        this.accommodationName = accommodationName;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.numberOfGuests = numberOfGuests;
    }
}
