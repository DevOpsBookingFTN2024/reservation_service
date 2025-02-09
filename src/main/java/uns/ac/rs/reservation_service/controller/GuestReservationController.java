package uns.ac.rs.reservation_service.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.request.CreateReservationRequest;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.GuestReservationService;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/reservations/guest")
public class GuestReservationController {
    @Autowired
    private GuestReservationService guestReservationService;

    @PostMapping("/create/{accommodationId}")
    public ResponseEntity<?> createReservationGuest(@PathVariable UUID accommodationId,
                             @Valid @RequestBody CreateReservationRequest createReservationRequest,
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Guest is creating reservation for accommodation with ID: {}", accommodationId);
        MessageResponse messageResponse = guestReservationService
                .createReservationGuest(accommodationId, createReservationRequest, jwtToken);
        log.info("Reservation created successfully for accommodation with ID: {}", accommodationId);
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservationGuest(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Guest is attempting to cancel reservation with ID: {}", reservationId);
        MessageResponse messageResponse = guestReservationService.cancelReservationGuest(reservationId, jwtToken);
        log.info("Reservation with ID: {} cancelled successfully.", reservationId);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPendingReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching pending reservations for guest.");
        List<ReservationDTO> pendingReservations = guestReservationService
                .getMyPendingReservationsGuest(jwtToken, idAccommodation);
        log.info("Fetched {} pending reservations.", pendingReservations.size());
        return ResponseEntity.ok(pendingReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getMyAcceptedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching accepted reservations for guest.");
        List<ReservationDTO> acceptedReservations = guestReservationService
                .getMyAcceptedReservationsGuest(jwtToken, idAccommodation);
        log.info("Fetched {} accepted reservations.", acceptedReservations.size());
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/past")
    public ResponseEntity<?> getMyPastReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Fetching past reservations for guest.");
        List<ReservationDTO> pastReservations = guestReservationService
                .getMyPastReservationsGuest(jwtToken, idAccommodation);
        log.info("Fetched {} past reservations.", pastReservations.size());
        return ResponseEntity.ok(pastReservations);
    }

    //endpoint koristi RatingService
    @GetMapping("/has-successfully-passed-host/{host}")
    public ResponseEntity<?> isGuestHasSuccessfullyPassedReservationHost(@PathVariable String host,
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Checking if guest has successfully completed reservation for host");
        boolean result = guestReservationService.isGuestHasSuccessfullyPassedReservationHost(host, jwtToken);
        log.info("Guest {} successfully passed reservation for host", result);
        return ResponseEntity.ok(result);
    }

    //endpoint koristi RatingService
    @GetMapping("/has-successfully-passed-accommodation/{idAccommodation}")
    public ResponseEntity<?> isGuestHasSuccessfullyPassedReservationAccommodation(@PathVariable UUID idAccommodation,
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Checking if guest has successfully completed reservation for accommodation");
        boolean result = guestReservationService
                .isGuestHasSuccessfullyPassedReservationAccommodation(idAccommodation, jwtToken);
        log.info("Guest {} successfully passed reservation for accommodation", result);
        return ResponseEntity.ok(result);
    }

    //endpoint koristi UserService
    @GetMapping("/has-accepted-reservation/{guest}")
    public ResponseEntity<?> isGuestHasAcceptedReservation(@PathVariable String guest) {
        log.info("Checking if guest {} has an accepted reservation.", guest);
        boolean result = guestReservationService.isGuestHasAcceptedReservation(guest);
        log.info("Guest {} has accepted reservation: {}", guest, result);
        return ResponseEntity.ok(result);
    }

    //endpoint koristi UserService
    @PutMapping("/cancel-pending-reservations")
    public ResponseEntity<?> cancelMyPendingReservationsGuest(
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        log.info("Guest is attempting to cancel all pending reservations.");
        MessageResponse messageResponse = guestReservationService.cancelMyPendingReservationsGuest(jwtToken);
        log.info("All pending reservations for guest have been cancelled.");
        return ResponseEntity.ok(messageResponse);
    }
}
