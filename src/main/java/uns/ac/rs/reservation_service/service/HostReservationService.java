package uns.ac.rs.reservation_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.dto.client.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.AvailabilityDTO;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.client.CreateNotificationRequest;
import uns.ac.rs.reservation_service.dto.client.UserDTO;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.mapper.ReservationMapper;
import uns.ac.rs.reservation_service.model.EReservationStatus;
import uns.ac.rs.reservation_service.model.Reservation;
import uns.ac.rs.reservation_service.model.client.ENotificationType;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import uns.ac.rs.reservation_service.service.client.AccommodationServiceClient;
import uns.ac.rs.reservation_service.service.client.NotificationServiceClient;
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

    private final NotificationServiceClient notificationServiceClient;

    public HostReservationService(ReservationRepository reservationRepository,
                                  UserServiceClient userServiceClient,
                                  AccommodationServiceClient accommodationServiceClient,
                                  NotificationServiceClient notificationServiceClient) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
        this.accommodationServiceClient = accommodationServiceClient;
        this.notificationServiceClient = notificationServiceClient;
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

        List<AvailabilityDTO> convertedAvailabilities = selectedAvailabilities
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (reservation.getReservationStatus() == EReservationStatus.PENDING) {
            List<ReservationDTO> reservationsToCheck =
                    getAllPendingReservationsByAccommodation(reservation.getIdAccommodation());

            for (ReservationDTO dto : reservationsToCheck) {
                if (dto.getId().equals(reservation.getId())) continue;

                if (reservationsOverlap(reservation.getDateFrom(),
                                        reservation.getDateTo(),
                                        dto.getDateFrom(),
                                        dto.getDateTo())) {
                    Reservation reservationToDecline = reservationRepository.findById(dto.getId())
                            .orElseThrow(() ->
                                    new NoSuchElementException("Reservation not found with id: " + dto.getId()));

                    reservationToDecline.setReservationStatus(EReservationStatus.DECLINED);

                    reservationRepository.save(reservationToDecline);

                    CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest(
                            reservation.getGuest(),
                            "Host "
                                    + userDetails.getUsername()
                                    + " declined your reservation request for accommodation "
                                    + reservation.getAccommodationName()
                                    + ".",
                            ENotificationType.RESERVATION_RESPONSE.name()
                    );

                    notificationServiceClient.createNotification(createNotificationRequest, jwtToken);
                }
            }

            accommodationServiceClient.reserveAvailabilities(convertedAvailabilities, jwtToken);

            reservation.setReservationStatus(EReservationStatus.ACCEPTED);

            reservationRepository.save(reservation);

            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest(
                    reservation.getGuest(),
                    "Host "
                            + userDetails.getUsername()
                            + " accepted your reservation request for accommodation "
                            + reservation.getAccommodationName()
                            + ".",
                    ENotificationType.RESERVATION_RESPONSE.name()
            );

            notificationServiceClient.createNotification(createNotificationRequest, jwtToken);

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

        if (reservation.getReservationStatus() == EReservationStatus.PENDING) {
            reservation.setReservationStatus(EReservationStatus.DECLINED);

            reservationRepository.save(reservation);

            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest(
                    reservation.getGuest(),
                    "Host "
                            + userDetails.getUsername()
                            + " declined your reservation request for accommodation "
                            + reservation.getAccommodationName()
                            + ".",
                    ENotificationType.RESERVATION_RESPONSE.name()
            );

            notificationServiceClient.createNotification(createNotificationRequest, jwtToken);

            return new MessageResponse("Reservation declined successfully.");
        } else {
            throw new SecurityException("You cannot decline this reservation.");
        }
    }

    //rezervacija je na cekanju
    public List<ReservationDTO> getMyPendingReservationsHost(String jwtToken, UUID accommodationId) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByHost(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.PENDING)
                .filter(reservation -> accommodationId == null ||
                        reservation.getIdAccommodation().equals(accommodationId))
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    Integer canceledReservations = canceledReservationsNumberGuest(reservation.getGuest());

                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails, canceledReservations);
                })
                .toList();
    }

    //rezervacija je prihvacena
    public List<ReservationDTO> getMyAcceptedReservationsHost(String jwtToken, UUID accommodationId) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByHost(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.ACCEPTED)
                .filter(reservation -> accommodationId == null ||
                        reservation.getIdAccommodation().equals(accommodationId))
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails);
                })
                .toList();
    }

    //rezervacija je odbijena
    //rezervacija je otkazana
    //rezervacija je uspesno prosla
    public List<ReservationDTO> getMyPastReservationsHost(String jwtToken, UUID accommodationId) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByHost(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.DECLINED ||
                                       reservation.getReservationStatus() == EReservationStatus.CANCELLED ||
                                       reservation.getReservationStatus() == EReservationStatus.PASSED)
                .filter(reservation -> accommodationId == null ||
                        reservation.getIdAccommodation().equals(accommodationId))
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails);
                })
                .toList();
    }

    //metodu koristi UserService
    public boolean isHostHasAcceptedReservation(String host) {
        List<Reservation> hostAcceptedReservations = reservationRepository
                .findByHostAndReservationStatus(host, EReservationStatus.ACCEPTED);

        return !hostAcceptedReservations.isEmpty();
    }

    //metodu koristi UserService
    public MessageResponse declineMyPendingReservationsHost(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_HOST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        List<Reservation> hostPendingReservations = reservationRepository
                .findByHostAndReservationStatus(userDetails.getUsername(), EReservationStatus.PENDING);

        for (Reservation reservation : hostPendingReservations) {
            reservation.setReservationStatus(EReservationStatus.DECLINED);
        }

        reservationRepository.saveAll(hostPendingReservations);

        return new MessageResponse("Reservations declined successfully.");
    }

    //metodu koristi AccommodationService
    public boolean isAccommodationHasAcceptedReservation(UUID idAccommodation) {
        List<Reservation> accommodationAcceptedReservations = reservationRepository
                .findByIdAccommodationAndReservationStatus(idAccommodation, EReservationStatus.ACCEPTED);

        return !accommodationAcceptedReservations.isEmpty();
    }

    private List<ReservationDTO> getAllPendingReservationsByAccommodation(UUID accommodationId) {
        return reservationRepository.findByIdAccommodation(accommodationId)
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.PENDING)
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails);
                })
                .toList();
    }

    private boolean reservationsOverlap(LocalDate startDate1,
                                        LocalDate endDate1,
                                        LocalDate startDate2,
                                        LocalDate endDate2) {
        return startDate1.isBefore(endDate2) && endDate1.isAfter(startDate2);
    }

    private Integer canceledReservationsNumberGuest(String guest) {
        List<Reservation> canceledReservations = reservationRepository
                .findByGuestAndReservationStatus(guest, EReservationStatus.CANCELLED);
        return canceledReservations.size();
    }
}
