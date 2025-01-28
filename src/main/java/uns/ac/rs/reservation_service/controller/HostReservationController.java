package uns.ac.rs.reservation_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.HostReservationService;
import java.util.List;
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
        MessageResponse messageResponse = hostReservationService
                .acceptReservationHost(reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/decline/{reservationId}")
    public ResponseEntity<?> declineReservationHost(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = hostReservationService
                .declineReservationHost(reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPendingReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> pendingReservations = hostReservationService
                .getMyPendingReservationsHost(jwtToken, idAccommodation);
        return ResponseEntity.ok(pendingReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getMyAcceptedReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> acceptedReservations = hostReservationService
                .getMyAcceptedReservationsHost(jwtToken, idAccommodation);
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/past")
    public ResponseEntity<?> getMyPastReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> declinedReservations = hostReservationService
                .getMyPastReservationsHost(jwtToken, idAccommodation);
        return ResponseEntity.ok(declinedReservations);
    }

    //endpoint koristi UserService
    @GetMapping("/has-accepted-reservation/{host}")
    public ResponseEntity<?> isHostHasAcceptedReservation(@PathVariable String host) {
        boolean result = hostReservationService.isHostHasAcceptedReservation(host);
        return ResponseEntity.ok(result);
    }

    //endpoint koristi UserService
    @PutMapping("/decline-pending-reservations")
    public ResponseEntity<?> declineMyPendingReservationsHost(
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = hostReservationService.declineMyPendingReservationsHost(jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    //endpoint koristi AccommodationService
    @GetMapping("/accommodation/has-accepted-reservation/{idAccommodation}")
    public ResponseEntity<?> isAccommodationHasAcceptedReservation(@PathVariable UUID idAccommodation) {
        boolean result = hostReservationService.isAccommodationHasAcceptedReservation(idAccommodation);
        return ResponseEntity.ok(result);
    }
}
