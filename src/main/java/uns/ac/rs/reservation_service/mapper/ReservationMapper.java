package uns.ac.rs.reservation_service.mapper;

import uns.ac.rs.reservation_service.dto.client.AccommodationDTO;
import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.model.Reservation;

public class ReservationMapper {
    public static ReservationDTO toReservationDTO(Reservation reservation,
                                                  AccommodationDTO accommodationDTO) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .guest(reservation.getGuest())
                .host(reservation.getHost())
                .idAccommodation(reservation.getIdAccommodation())
                .accommodationName(reservation.getAccommodationName())
                .dateFrom(reservation.getDateFrom())
                .dateTo(reservation.getDateTo())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .reservationStatus(reservation.getReservationStatus().name())
                .accommodation(accommodationDTO)
                .canceledReservations(null)
                .build();
    }

    public static ReservationDTO toReservationDTO(Reservation reservation,
                                                  AccommodationDTO accommodationDTO,
                                                  Integer canceledReservations) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .guest(reservation.getGuest())
                .host(reservation.getHost())
                .idAccommodation(reservation.getIdAccommodation())
                .accommodationName(reservation.getAccommodationName())
                .dateFrom(reservation.getDateFrom())
                .dateTo(reservation.getDateTo())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .reservationStatus(reservation.getReservationStatus().name())
                .accommodation(accommodationDTO)
                .canceledReservations(canceledReservations)
                .build();
    }
}
