package uns.ac.rs.reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private UUID id;

    private String guest;

    private UUID idAccommodation;

    private Set<LocalDate> dates;

    private Integer numberOfGuests;

    private Double totalPrice;

    private Boolean isAccepted;

    private Boolean isDeclined;

    private Boolean isCancelled;
}
