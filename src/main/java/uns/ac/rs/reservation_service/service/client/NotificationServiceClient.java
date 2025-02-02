package uns.ac.rs.reservation_service.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uns.ac.rs.reservation_service.dto.client.CreateNotificationRequest;

@Service
public class NotificationServiceClient {
    private final WebClient webClient;

    @Autowired
    public NotificationServiceClient(WebClient.Builder webClientBuilder,
                                     @Value("${notification.service.url}") String notificationServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(notificationServiceUrl).build();
    }

    public void createNotification(CreateNotificationRequest createNotificationRequest, String jwtToken) {
        try {
            webClient.post()
                    .uri("/notifications/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .bodyValue(createNotificationRequest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to NotificationService: ", e);
        }
    }
}
