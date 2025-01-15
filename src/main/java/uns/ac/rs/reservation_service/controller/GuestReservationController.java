package uns.ac.rs.reservation_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.service.GuestReservationService;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservations/guest")
public class GuestReservationController {
    @Autowired
    private GuestReservationService guestReservationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPendingReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> pendingReservations = guestReservationService.getMyPendingReservationsGuest(jwtToken);
        return ResponseEntity.ok(pendingReservations);
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> getMyAcceptedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> acceptedReservations = guestReservationService.getMyAcceptedReservationsGuest(jwtToken);
        return ResponseEntity.ok(acceptedReservations);
    }

    @GetMapping("/declined")
    public ResponseEntity<?> getMyDeclinedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> declinedReservations = guestReservationService.getMyDeclinedReservationsGuest(jwtToken);
        return ResponseEntity.ok(declinedReservations);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<?> getMyCancelledReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> cancelledReservations = guestReservationService.getMyCancelledReservationsGuest(jwtToken);
        return ResponseEntity.ok(cancelledReservations);
    }

    @GetMapping("/passed")
    public ResponseEntity<?> getMyPassedReservationsGuest(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        List<ReservationDTO> passedReservations = guestReservationService.getMyPassedReservationsGuest(jwtToken);
        return ResponseEntity.ok(passedReservations);
    }
}
