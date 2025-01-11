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
public class AvailabilityDTO {
    private UUID id;

    private LocalDate date;

    private Boolean isAvailable;

    private Boolean isReserved;

    private Double pricePerGuest;

    private Double pricePerUnit;
}
