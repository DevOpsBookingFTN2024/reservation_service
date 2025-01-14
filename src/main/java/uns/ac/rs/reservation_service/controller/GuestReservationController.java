package uns.ac.rs.reservation_service.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.request.CreateReservationRequest;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.service.GuestReservationService;
import uns.ac.rs.reservation_service.service.ReservationService;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservations/quest")
public class GuestReservationController {

    @Autowired
    private GuestReservationService guestReservationService;

    @GetMapping("/cancelled")
    public ResponseEntity<?> getCancelledReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> cancelledReservations = guestReservationService.getMyCancelledReservationsGuest(jwtToken);
        return ResponseEntity.ok(cancelledReservations);
    }

    @GetMapping("/declined")
    public ResponseEntity<?> getDeclinedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> declinedReservations = guestReservationService.getMyDeclinedReservationsGuest(jwtToken);
        return ResponseEntity.ok(declinedReservations);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> activeReservations = guestReservationService.getMyActiveReservationsGuest(jwtToken);
        return ResponseEntity.ok(activeReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getAcceptedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> acceptedReservations = guestReservationService.getMyAcceptedReservationsGuest(jwtToken);
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/passed")
    public ResponseEntity<?> getPassedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> passedReservations = guestReservationService.getMyPassedReservationsGuest(jwtToken);
        return ResponseEntity.ok(passedReservations);
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservationGuest(@PathVariable UUID reservationId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        MessageResponse messageResponse = guestReservationService.cancelReservationGuest(reservationId, jwtToken);
        return ResponseEntity.ok(messageResponse);
    }
}
