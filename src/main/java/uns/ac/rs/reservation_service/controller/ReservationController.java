package uns.ac.rs.reservation_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.request.CreateReservationRequest;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.ReservationService;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping("/create/{accommodationId}")
    public ResponseEntity<?> createReservation(@PathVariable UUID accommodationId,
                                               @Valid @RequestBody CreateReservationRequest createReservationRequest,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = reservationService.createReservation(
                accommodationId, createReservationRequest, jwtToken);
        return ResponseEntity.ok(messageResponse );
    }
}
