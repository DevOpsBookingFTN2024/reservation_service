package uns.ac.rs.reservation_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.HostReservationService;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservations/host")
public class HostReservationController {
    @Autowired
    private HostReservationService hostReservationService;

    @PutMapping("/accept/{reservationId}")
    public ResponseEntity<?> acceptReservationHost(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = hostReservationService.acceptReservationHost(
                reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/decline/{reservationId}")
    public ResponseEntity<?> declineReservationHost(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = hostReservationService.declineReservationHost(
                reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }
}
