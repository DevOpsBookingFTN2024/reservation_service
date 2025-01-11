package uns.ac.rs.reservation_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateReservationRequest {
    @NotNull(message = "Start date is required.")
    private LocalDate dateFrom;

    @NotNull(message = "End date is required.")
    private LocalDate dateTo;

    @NotNull(message = "Number of guests is required.")
    @Min(value = 1, message = "Number of guests must be at least 1.")
    private Integer numberOfGuests;
}
