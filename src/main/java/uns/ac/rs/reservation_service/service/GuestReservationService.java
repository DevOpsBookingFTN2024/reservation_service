package uns.ac.rs.reservation_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.mapper.ReservationMapper;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import uns.ac.rs.reservation_service.dto.UserDTO;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class GuestReservationService {
    private final ReservationRepository reservationRepository;
    private final UserServiceClient userServiceClient;

    public GuestReservationService(ReservationRepository reservationRepository,
                                   UserServiceClient userServiceClient) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
    }

    public List<ReservationDTO> getMyPendingReservationsGuest(String jwtToken) {
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
                .filter(reservation -> reservation.getDateFrom().isAfter(LocalDate.now()))
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
                .filter(reservation -> Boolean.FALSE.equals(reservation.getIsCancelled()))
                .filter(reservation -> !reservation.getDateTo().isBefore(LocalDate.now()))
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
                .filter(reservation -> reservation.getDateTo().isBefore(LocalDate.now()))
                .map(ReservationMapper::toReservationDTO)
                .toList();
    }
}
