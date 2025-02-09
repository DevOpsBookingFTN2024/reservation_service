package uns.ac.rs.reservation_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.HostReservationService;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/reservations/host")
public class HostReservationController {
    @Autowired
    private HostReservationService hostReservationService;

    @PutMapping("/accept/{reservationId}")
    public ResponseEntity<?> acceptReservationHost(@PathVariable UUID reservationId,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Host is accepting reservation for with ID: {}", reservationId);
        MessageResponse messageResponse = hostReservationService
                .acceptReservationHost(reservationId, jwtToken);
        log.info("Reservation with ID: {} accepted successfully.", reservationId);
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/decline/{reservationId}")
    public ResponseEntity<?> declineReservationHost(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Host is attempting to decline reservation with ID: {}", reservationId);
        MessageResponse messageResponse = hostReservationService
                .declineReservationHost(reservationId, jwtToken);
        log.info("Reservation with ID: {} declined successfully.", reservationId);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPendingReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching pending reservations for host.");
        List<ReservationDTO> pendingReservations = hostReservationService
                .getMyPendingReservationsHost(jwtToken, idAccommodation);
        log.info("Fetched {} pending reservations.", pendingReservations.size());
        return ResponseEntity.ok(pendingReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getMyAcceptedReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching accepted reservations for host.");
        List<ReservationDTO> acceptedReservations = hostReservationService
                .getMyAcceptedReservationsHost(jwtToken, idAccommodation);
        log.info("Fetched {} accepted reservations.", acceptedReservations.size());
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/past")
    public ResponseEntity<?> getMyPastReservationsHost(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching past reservations for host.");
        List<ReservationDTO> pastReservations = hostReservationService
                .getMyPastReservationsHost(jwtToken, idAccommodation);
        log.info("Fetched {} past reservations.", pastReservations.size());
        return ResponseEntity.ok(pastReservations);
    }

    //endpoint koristi UserService
    @GetMapping("/has-accepted-reservation/{host}")
    public ResponseEntity<?> isHostHasAcceptedReservation(@PathVariable String host) {
        log.info("Checking if host {} has an accepted reservation.", host);
        boolean result = hostReservationService.isHostHasAcceptedReservation(host);
        log.info("Host {} has accepted reservation: {}", host, result);
        return ResponseEntity.ok(result);
    }

    //endpoint koristi UserService
    @PutMapping("/decline-pending-reservations")
    public ResponseEntity<?> declineMyPendingReservationsHost(
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Host is attempting to decline all pending reservations.");
        MessageResponse messageResponse = hostReservationService.declineMyPendingReservationsHost(jwtToken);
        log.info("All pending reservations for host have been declined.");
        return ResponseEntity.ok(messageResponse);
    }

    //endpoint koristi AccommodationService
    @GetMapping("/accommodation/has-accepted-reservation/{idAccommodation}")
    public ResponseEntity<?> isAccommodationHasAcceptedReservation(@PathVariable UUID idAccommodation) {
        log.info("Checking if host's accommodation has an accepted reservation.");
        boolean result = hostReservationService.isAccommodationHasAcceptedReservation(idAccommodation);
        log.info("Host's accommodation has accepted reservation: {}", result);
        return ResponseEntity.ok(result);
    }
}
