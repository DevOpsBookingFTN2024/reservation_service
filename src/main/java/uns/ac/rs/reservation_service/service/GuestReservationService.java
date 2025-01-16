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
                .datesUntil(createReservationRequest.getDateTo().plusDays(1))
                .collect(Collectors.toSet());
        ////////////////////////////////////////
        System.out.println(dates);
        ////////////////////////////////////////
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

        Reservation newReservation = new Reservation(
                userDetails.getUsername(),
                accommodationId,
                createReservationRequest.getDateFrom(),
                createReservationRequest.getDateTo(),
                createReservationRequest.getNumberOfGuests()
        );

        Double totalPrice = 0.0;
        //izmeniti
        ////////////////////////////////////////
        Double pricePerGuest = convertedAvailabilities.stream()
                .map(AvailabilityDTO::getPricePerGuest)
                .findFirst()
                .orElse(null);

        Double pricePerUnit = convertedAvailabilities.stream()
                .map(AvailabilityDTO::getPricePerUnit)
                .findFirst()
                .orElse(null);

        if (accommodationDetails.getPricingStrategy().equals("PER_UNIT")){
            totalPrice = convertedAvailabilities.stream()
                    .mapToDouble(AvailabilityDTO::getPricePerUnit)
                    .sum();
        } else {
            if (pricePerGuest != null)
                totalPrice = pricePerGuest * createReservationRequest.getNumberOfGuests() * dates.size();
        }
        ////////////////////////////////////////
        newReservation.setTotalPrice(totalPrice);

        if (Objects.equals(accommodationDetails.getApprovalStrategy(), "AUTOMATIC")) {
            accommodationServiceClient.reserveAvailabilities(convertedAvailabilities, jwtToken);
            newReservation.setIsAccepted(true);
        } else {
            newReservation.setIsAccepted(false);
        }

        newReservation.setIsDeclined(false);
        newReservation.setIsCancelled(false);

        reservationRepository.save(newReservation);
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

        if (reservation.getIsAccepted() && !reservation.getIsCancelled() &&
                LocalDate.now().isBefore(reservation.getDateFrom().minusDays(2))) {
            accommodationServiceClient.releaseAvailabilities(reservation.getIdAccommodation(),
                    reservation.getDateFrom(), reservation.getDateTo(), jwtToken);
            reservation.setIsCancelled(true);

            reservationRepository.save(reservation);
            return new MessageResponse("Reservation cancelled successfully.");
        } else {
            throw new SecurityException("You cannot cancel this reservation.");
        }
    }

    //rezervacija nije prihvacena
    //rezervacija nije odbijena
    //rezervacija nije otkazana
    //datum pocetka rezervacije je posle danasnjeg datuma
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

    //rezervacija je prihvacena
    //rezervacija nije otkazana
    //datum kraja rezervacije nije pre danasnjeg datuma
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

    //rezervacija je odbijena
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

    //rezervacija je otkazana
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

    //rezervacija je prihvacena
    //rezervacija nije otkazana
    //datum kraja rezervacije je pre danasnjeg datuma
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
