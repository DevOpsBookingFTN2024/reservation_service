package uns.ac.rs.reservation_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.dto.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.AvailabilityDTO;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.UserDTO;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.mapper.ReservationMapper;
import uns.ac.rs.reservation_service.model.EReservationStatus;
import uns.ac.rs.reservation_service.model.Reservation;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import uns.ac.rs.reservation_service.service.client.AccommodationServiceClient;
import uns.ac.rs.reservation_service.service.client.UserServiceClient;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class HostReservationService {
    private final ReservationRepository reservationRepository;
    private final UserServiceClient userServiceClient;
    private final AccommodationServiceClient accommodationServiceClient;

    public HostReservationService(ReservationRepository reservationRepository,
                              UserServiceClient userServiceClient,
                              AccommodationServiceClient accommodationServiceClient) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
        this.accommodationServiceClient = accommodationServiceClient;
    }

    public MessageResponse acceptReservationHost(UUID reservationId, String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found with id: " + reservationId));

        AccommodationDTO accommodationDetails = accommodationServiceClient
                .getAccommodationDetails(reservation.getIdAccommodation());
        if (accommodationDetails == null) {
            throw new IllegalStateException("Accommodation details could not be retrieved.");
        }
        if (!Objects.equals(accommodationDetails.getHost(), userDetails.getUsername())) {
            throw new SecurityException("User is not the owner of this accommodation.");
        }

        Set<LocalDate> dates = reservation.getDateFrom()
                .datesUntil(reservation.getDateTo())
                .collect(Collectors.toSet());

        List<AvailabilityDTO> availabilities = accommodationServiceClient
                .getAvailabilitiesDetailsByAccommodation(reservation.getIdAccommodation());

        List<Optional<AvailabilityDTO>> selectedAvailabilities = new ArrayList<>();
        for (LocalDate date : dates) {
            Optional<AvailabilityDTO> availability = availabilities.stream()
                    .filter(a -> a.getDate().equals(date))
                    .findFirst();

            if (availability.isEmpty()) {
                throw new NoSuchElementException("No availability data found for the date: " + date);
            }

            if (!availability.get().getIsAvailable()) {
                throw new SecurityException("The date " + date + " is not available.");
            }

            selectedAvailabilities.add(availability);
        }

        List<AvailabilityDTO> convertedAvailabilities = selectedAvailabilities.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if ((reservation.getReservationStatus() == EReservationStatus.PENDING) &&
                reservation.getDateFrom().isAfter(LocalDate.now())) {
            List<ReservationDTO> reservationsToCheck =
                    getAllPendingReservationsByAccommodation(reservation.getIdAccommodation());

            for (ReservationDTO dto : reservationsToCheck) {
                if (dto.getId().equals(reservation.getId())) continue;

                if (reservationsOverlap(reservation.getDateFrom(), reservation.getDateTo(),
                                        dto.getDateFrom(), dto.getDateTo())) {
                    Reservation reservationToDecline = reservationRepository.findById(dto.getId())
                            .orElseThrow(() ->
                                    new NoSuchElementException("Reservation not found with id: " + dto.getId()));

                    reservationToDecline.setReservationStatus(EReservationStatus.DECLINED);

                    reservationRepository.save(reservationToDecline);
                }
            }

            accommodationServiceClient.reserveAvailabilities(convertedAvailabilities, jwtToken);

            reservation.setReservationStatus(EReservationStatus.ACCEPTED);

            reservationRepository.save(reservation);
            return new MessageResponse("Reservation accepted successfully.");
        } else {
            throw new SecurityException("You cannot accept this reservation.");
        }
    }

    public MessageResponse declineReservationHost(UUID reservationId, String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found with id: " + reservationId));

        AccommodationDTO accommodationDetails = accommodationServiceClient
                .getAccommodationDetails(reservation.getIdAccommodation());
        if (accommodationDetails == null) {
            throw new IllegalStateException("Accommodation details could not be retrieved.");
        }

        if (!Objects.equals(accommodationDetails.getHost(), userDetails.getUsername())) {
            throw new SecurityException("User is not the owner of this accommodation.");
        }

        if ((reservation.getReservationStatus() == EReservationStatus.PENDING) &&
                reservation.getDateFrom().isAfter(LocalDate.now())) {
            reservation.setReservationStatus(EReservationStatus.DECLINED);

            reservationRepository.save(reservation);
            return new MessageResponse("Reservation declined successfully.");
        } else {
            throw new SecurityException("You cannot decline this reservation.");
        }
    }

    //rezervacija je na cekanju
    //datum pocetka rezervacije je posle danasnjeg datuma
    public List<ReservationDTO> getAllPendingReservationsByAccommodation(UUID accommodationId) {
        return reservationRepository.findByIdAccommodation(accommodationId)
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.PENDING)
                .filter(reservation -> reservation.getDateFrom().isAfter(LocalDate.now()))
                .map(ReservationMapper::toReservationDTO)
                .collect(Collectors.toList());
    }

    private boolean reservationsOverlap(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
        return startDate1.isBefore(endDate2) && endDate1.isAfter(startDate2);
    }
}
