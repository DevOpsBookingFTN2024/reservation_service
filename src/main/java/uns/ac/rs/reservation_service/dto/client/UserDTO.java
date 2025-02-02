package uns.ac.rs.reservation_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;

    private String username;

    private String emailAddress;

    private String firstName;

    private String lastName;

    private String residence;

    private Set<String> roles;
}
