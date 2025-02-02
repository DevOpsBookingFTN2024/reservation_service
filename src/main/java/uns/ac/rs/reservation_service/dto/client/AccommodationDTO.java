package uns.ac.rs.reservation_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationDTO {
    private UUID id;

    private String name;

    private String host;

    private String address;

    private String city;

    private String country;

    private Integer minimumGuests;

    private Integer maximumGuests;

    private String pricingStrategy;

    private String approvalStrategy;
}
