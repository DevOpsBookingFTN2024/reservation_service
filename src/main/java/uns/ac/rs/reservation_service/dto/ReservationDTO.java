package uns.ac.rs.reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private UUID id;

    private String guest;

    private String host;

    private UUID idAccommodation;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Integer numberOfGuests;

    private Double totalPrice;

    private String reservationStatus;

    private AccommodationDTO accommodation;

    private Integer canceledReservations;
}
