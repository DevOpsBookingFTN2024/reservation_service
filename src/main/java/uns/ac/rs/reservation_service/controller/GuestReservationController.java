package uns.ac.rs.reservation_service.controller;

import jakarta.validation.Valid;
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
        MessageResponse messageResponse = guestReservationService.createReservationGuest(
                accommodationId, createReservationRequest, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservationGuest(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = guestReservationService.cancelReservationGuest(reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPendingReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> pendingReservations = guestReservationService
                .getMyPendingReservationsGuest(jwtToken, idAccommodation);
        return ResponseEntity.ok(pendingReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getMyAcceptedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> acceptedReservations = guestReservationService
                .getMyAcceptedReservationsGuest(jwtToken, idAccommodation);
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/past")
    public ResponseEntity<?> getMyPastReservationsGuest(@RequestHeader("Authorization") String authorizationHeader,
                             @RequestParam(value = "idAccommodation", required = false) UUID idAccommodation) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> declinedReservations = guestReservationService
                .getMyPastReservationsGuest(jwtToken, idAccommodation);
        return ResponseEntity.ok(declinedReservations);
    }

    @GetMapping("/has-successfully-passed-host/{host}")
    public ResponseEntity<?> isGuestHasSuccessfullyPassedReservationHost(@PathVariable String host,
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        boolean result = guestReservationService.isGuestHasSuccessfullyPassedReservationHost(host, jwtToken);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/has-successfully-passed-accommodation/{idAccommodation}")
    public ResponseEntity<?> isGuestHasSuccessfullyPassedReservationAccommodation(@PathVariable UUID idAccommodation,
                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        boolean result = guestReservationService
                .isGuestHasSuccessfullyPassedReservationAccommodation(idAccommodation, jwtToken);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/has-accepted-reservation/{guest}")
    public ResponseEntity<?> isGuestHasAcceptedReservation(@PathVariable String guest) {
        boolean result = guestReservationService.isGuestHasAcceptedReservation(guest);
        return ResponseEntity.ok(result);
    }
}
