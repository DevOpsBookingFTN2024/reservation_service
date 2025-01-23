package uns.ac.rs.reservation_service.mapper;

import uns.ac.rs.reservation_service.dto.ReservationDTO;
import uns.ac.rs.reservation_service.model.Reservation;

public class ReservationMapper {
    public static ReservationDTO toReservationDTO(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .guest(reservation.getGuest())
                .idAccommodation(reservation.getIdAccommodation())
                .dateFrom(reservation.getDateFrom())
                .dateTo(reservation.getDateTo())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .reservationStatus(reservation.getReservationStatus().name())
                .build();
    }
}
