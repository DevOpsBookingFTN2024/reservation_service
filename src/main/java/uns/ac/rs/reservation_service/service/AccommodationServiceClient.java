package uns.ac.rs.reservation_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uns.ac.rs.reservation_service.dto.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.AvailabilityDTO;
import java.util.List;
import java.util.UUID;

@Service
public class AccommodationServiceClient {
    private final WebClient webClient;

    @Autowired
    public AccommodationServiceClient(WebClient.Builder webClientBuilder,
                                      @Value("${accommodation.service.url}") String userServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    }

    public AccommodationDTO getAccommodationDetails(UUID accommodationId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accommodations/{accommodationId}")
                            .build(accommodationId))
                    .retrieve()
                    .bodyToMono(AccommodationDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AccommodationService: ", e);
        }
    }

    public List<AvailabilityDTO> getAvailabilitiesDetailsByAccommodation(UUID accommodationId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/availabilities/all/{accommodationId}")
                            .build(accommodationId))
                    .retrieve()
                    .bodyToFlux(AvailabilityDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AccommodationService: ", e);
        }
    }

    public void reserveAvailabilities(List<AvailabilityDTO> availabilityDTOs, String jwtToken) {
        try {
            webClient.put()
                    .uri("/availabilities/reserve")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .bodyValue(availabilityDTOs)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AccommodationService: ", e);
        }
    }
}
