package uns.ac.rs.reservation_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.dto.client.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.AvailabilityDTO;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.dto.client.CreateNotificationRequest;
import uns.ac.rs.reservation_service.dto.request.CreateReservationRequest;
import uns.ac.rs.reservation_service.dto.response.MessageResponse;
import uns.ac.rs.reservation_service.mapper.ReservationMapper;
import uns.ac.rs.reservation_service.model.EReservationStatus;
import uns.ac.rs.reservation_service.model.Reservation;
import uns.ac.rs.reservation_service.model.client.ENotificationType;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import uns.ac.rs.reservation_service.dto.client.UserDTO;
import uns.ac.rs.reservation_service.service.client.AccommodationServiceClient;
import uns.ac.rs.reservation_service.service.client.NotificationServiceClient;
import uns.ac.rs.reservation_service.service.client.UserServiceClient;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuestReservationService {
    private final ReservationRepository reservationRepository;

    private final UserServiceClient userServiceClient;

    private final AccommodationServiceClient accommodationServiceClient;

    private final NotificationServiceClient notificationServiceClient;

    public GuestReservationService(ReservationRepository reservationRepository,
                              UserServiceClient userServiceClient,
                              AccommodationServiceClient accommodationServiceClient,
                              NotificationServiceClient notificationServiceClient) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
        this.accommodationServiceClient = accommodationServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    public MessageResponse createReservationGuest(UUID accommodationId,
                                                  CreateReservationRequest createReservationRequest,
                                                  String jwtToken) {
        if (createReservationRequest.getDateFrom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (!createReservationRequest.getDateTo().isAfter(createReservationRequest.getDateFrom())) {
            throw new IllegalArgumentException("End date must be after the start date.");
        }
        Set<LocalDate> dates = createReservationRequest.getDateFrom()
                .datesUntil(createReservationRequest.getDateTo())
                .collect(Collectors.toSet());

        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        AccommodationDTO accommodationDetails = accommodationServiceClient.getAccommodationDetails(accommodationId);
        if (accommodationDetails == null) {
            throw new IllegalStateException("Accommodation details could not be retrieved.");
        }

        if (createReservationRequest.getNumberOfGuests() < accommodationDetails.getMinimumGuests() ||
                createReservationRequest.getNumberOfGuests() > accommodationDetails.getMaximumGuests()) {
            throw new IllegalArgumentException(String.format("Number of guests must be between %d and %d.",
                    accommodationDetails.getMinimumGuests(),
                    accommodationDetails.getMaximumGuests()));
        }

        List<AvailabilityDTO> availabilities = accommodationServiceClient
                .getAvailabilitiesDetailsByAccommodation(accommodationId);

        List<Optional<AvailabilityDTO>> selectedAvailabilities = new ArrayList<>();
        for (LocalDate date : dates) {
            Optional<AvailabilityDTO> availability = availabilities
                    .stream()
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

        Reservation newReservation = new Reservation(
                userDetails.getUsername(),
                accommodationDetails.getHost(),
                accommodationId,
                accommodationDetails.getName(),
                createReservationRequest.getDateFrom(),
                createReservationRequest.getDateTo(),
                createReservationRequest.getNumberOfGuests()
        );

        Double totalPrice = 0.0;

        if (accommodationDetails.getPricingStrategy().equals("PER_UNIT")){
            totalPrice = convertedAvailabilities
                    .stream()
                    .mapToDouble(AvailabilityDTO::getPricePerUnit)
                    .sum();
        } else {
            totalPrice = convertedAvailabilities
                    .stream()
                    .mapToDouble(availability -> availability.getPricePerGuest() * createReservationRequest.getNumberOfGuests())
                    .sum();
        }
        newReservation.setTotalPrice(totalPrice);

        if (Objects.equals(accommodationDetails.getApprovalStrategy(), "AUTOMATIC")) {
            accommodationServiceClient.reserveAvailabilities(convertedAvailabilities, jwtToken);
            newReservation.setReservationStatus(EReservationStatus.ACCEPTED);
        } else {
            newReservation.setReservationStatus(EReservationStatus.PENDING);
        }

        reservationRepository.save(newReservation);

        CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest(
                newReservation.getHost(),
                "Guest "
                        + userDetails.getUsername()
                        + " created reservation request for your accommodation "
                        + newReservation.getAccommodationName()
                        + ".",
                ENotificationType.RESERVATION_REQUEST.name()
        );

        notificationServiceClient.createNotification(createNotificationRequest, jwtToken);

        return new MessageResponse("Reservation created successfully.");
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

        if ((reservation.getReservationStatus() == EReservationStatus.ACCEPTED &&
                LocalDate.now().isBefore(reservation.getDateFrom().minusDays(2))) ||
            (reservation.getReservationStatus() == EReservationStatus.PENDING &&
                LocalDate.now().isBefore(reservation.getDateFrom().minusDays(1)))) {
            accommodationServiceClient.releaseAvailabilities(
                    reservation.getIdAccommodation(),
                    reservation.getDateFrom(),
                    reservation.getDateTo(),
                    jwtToken
            );

            reservation.setReservationStatus(EReservationStatus.CANCELLED);

            reservationRepository.save(reservation);

            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest(
                    reservation.getHost(),
                    "Guest "
                            + userDetails.getUsername()
                            + " cancelled reservation request for your accommodation "
                            + reservation.getAccommodationName()
                            + ".",
                    ENotificationType.RESERVATION_CANCELED.name()
            );

            notificationServiceClient.createNotification(createNotificationRequest, jwtToken);

            return new MessageResponse("Reservation cancelled successfully.");
        } else {
            throw new SecurityException("You cannot cancel this reservation.");
        }
    }

    //rezervacija je na cekanju
    public List<ReservationDTO> getMyPendingReservationsGuest(String jwtToken, UUID idAccommodation) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.PENDING)
                .filter(reservation -> idAccommodation == null ||
                        reservation.getIdAccommodation().equals(idAccommodation))
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails);
                })
                .toList();
    }

    //rezervacija je prihvacena
    public List<ReservationDTO> getMyAcceptedReservationsGuest(String jwtToken, UUID idAccommodation) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.ACCEPTED)
                .filter(reservation -> idAccommodation == null ||
                        reservation.getIdAccommodation().equals(idAccommodation))
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
    public List<ReservationDTO> getMyPastReservationsGuest(String jwtToken, UUID idAccommodation) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        return reservationRepository.findByGuest(userDetails.getUsername())
                .stream()
                .filter(reservation -> reservation.getReservationStatus() == EReservationStatus.DECLINED ||
                                       reservation.getReservationStatus() == EReservationStatus.CANCELLED ||
                                       reservation.getReservationStatus() == EReservationStatus.PASSED)
                .filter(reservation -> idAccommodation == null ||
                        reservation.getIdAccommodation().equals(idAccommodation))
                .map(reservation -> {
                    AccommodationDTO accommodationDetails = accommodationServiceClient
                            .getAccommodationDetails(reservation.getIdAccommodation());
                    return ReservationMapper.toReservationDTO(reservation, accommodationDetails);
                })
                .toList();
    }

    //metodu koristi RatingService
    public boolean isGuestHasSuccessfullyPassedReservationHost(String host, String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        List<Reservation> hostSuccessfullyPassedReservations = reservationRepository
                .findByHostAndReservationStatus(host, EReservationStatus.PASSED);

        return hostSuccessfullyPassedReservations
                .stream()
                .anyMatch(reservation -> reservation.getGuest().equals(userDetails.getUsername()));
    }

    //metodu koristi RatingService
    public boolean isGuestHasSuccessfullyPassedReservationAccommodation(UUID idAccommodation, String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        List<Reservation> accommodationSuccessfullyPassedReservations = reservationRepository
                .findByIdAccommodationAndReservationStatus(idAccommodation, EReservationStatus.PASSED);

        return accommodationSuccessfullyPassedReservations
                .stream()
                .anyMatch(reservation -> reservation.getGuest().equals(userDetails.getUsername()));
    }

    //metodu koristi UserService
    public boolean isGuestHasAcceptedReservation(String guest) {
        List<Reservation> guestAcceptedReservations = reservationRepository
                .findByGuestAndReservationStatus(guest, EReservationStatus.ACCEPTED);

        return !guestAcceptedReservations.isEmpty();
    }

    //metodu koristi UserService
    public MessageResponse cancelMyPendingReservationsGuest(String jwtToken) {
        UserDTO userDetails = userServiceClient.getUserDetails(jwtToken);
        if (userDetails == null) {
            throw new IllegalStateException("User details could not be retrieved.");
        }
        if (!userDetails.getRoles().contains("ROLE_GUEST")) {
            throw new SecurityException("User do not have permission for this action.");
        }

        List<Reservation> guestPendingReservations = reservationRepository
                .findByGuestAndReservationStatus(userDetails.getUsername(), EReservationStatus.PENDING);

        for (Reservation reservation : guestPendingReservations) {
            reservation.setReservationStatus(EReservationStatus.CANCELLED);
        }

        reservationRepository.saveAll(guestPendingReservations);

        return new MessageResponse("Reservations cancelled successfully.");
    }
}
