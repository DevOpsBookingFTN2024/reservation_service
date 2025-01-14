package uns.ac.rs.reservation_service.service;



import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.dto.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.AvailabilityDTO;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.request.CreateReservationRequest;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.mapper.ReservationMapper;
import uns.ac.rs.reservation_service.model.Reservation;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import uns.ac.rs.reservation_service.dto.UserDTO;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuestReservationService {

    private final ReservationRepository reservationRepository;
    private final UserServiceClient userServiceClient;
    private final AccommodationServiceClient accommodationServiceClient;

    public GuestReservationService(ReservationRepository reservationRepository,
                                   UserServiceClient userServiceClient,
                                   AccommodationServiceClient accommodationServiceClient) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
        this.accommodationServiceClient = accommodationServiceClient;
    }

    public List<ReservationDTO> getMyCancelledReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> Boolean.TRUE.equals(reservation.getIsCancelled()))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }

    public List<ReservationDTO> getMyDeclinedReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> Boolean.TRUE.equals(reservation.getIsDeclined()))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }

    public List<ReservationDTO> getMyActiveReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> Boolean.FALSE.equals(reservation.getIsAccepted()))
                .filter(reservation -> Boolean.FALSE.equals(reservation.getIsDeclined()))
                .filter(reservation -> Boolean.FALSE.equals(reservation.getIsCancelled()))
                .filter(reservation -> reservation.getDates().stream()
                        .allMatch(date -> date.isAfter(LocalDate.now())))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }

    public List<ReservationDTO> getMyAcceptedReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> Boolean.TRUE.equals(reservation.getIsAccepted()))
                .filter(reservation -> reservation.getDates().stream()
                        .anyMatch(date -> !date.isBefore(LocalDate.now())))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }

    public List<ReservationDTO> getMyPassedReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> Boolean.TRUE.equals(reservation.getIsAccepted()))
                .filter(reservation -> Boolean.FALSE.equals(reservation.getIsCancelled()))
                .filter(reservation -> reservation.getDates().stream()
                        .allMatch(date -> date.isBefore(LocalDate.now())))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }

    public MessageResponse cancelReservationGuest(UUID reservationId, String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found with id: " + reservationId));

        if (!Objects.equals(reservation.getGuest(), userDetails.getUsername())) {
            throw new SecurityException("User did not create this reservation.");
        }

        if (reservation.getIsAccepted() &&
                !reservation.getIsCancelled() &&
                LocalDate.now().isBefore(reservation.getDates().stream().min(LocalDate::compareTo)
                        .orElse(LocalDate.MAX).minusDays(2))) {
            reservation.setIsCancelled(true);

            reservationRepository.save(reservation);
            return new MessageResponse("Reservation cancelled successfully.");
        } else if (!reservation.getIsAccepted() &&
                !reservation.getIsDeclined() &&
                !reservation.getIsCancelled() &&
                reservation.getDates().stream().allMatch(date -> date.isAfter(LocalDate.now()))) {
            reservation.setIsCancelled(true);

            reservationRepository.save(reservation);
            return new MessageResponse("Reservation cancelled successfully.");
        } else {
            throw new SecurityException("You cannot cancel this reservation.");
        }
    }

}
