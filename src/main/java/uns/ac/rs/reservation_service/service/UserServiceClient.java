package uns.ac.rs.reservation_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uns.ac.rs.reservation_service.dto.UserDTO;

@Service
public class UserServiceClient {
    private final WebClient webClient;

    @Autowired
    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${user.service.url}") String userServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    }

    public UserDTO getUserDetails(String jwtToken) {
        try {
            return webClient.get()
                    .uri("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to UserService: ", e);
        }
    }
}
