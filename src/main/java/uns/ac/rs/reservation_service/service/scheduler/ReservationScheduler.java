package uns.ac.rs.reservation_service.service.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uns.ac.rs.reservation_service.model.EReservationStatus;
import uns.ac.rs.reservation_service.model.Reservation;
import uns.ac.rs.reservation_service.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationScheduler {
    private final ReservationRepository reservationRepository;

    public ReservationScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void declineUnansweredReservations() {
        List<Reservation> pendingReservations = reservationRepository
                .findByReservationStatus(EReservationStatus.PENDING);

        for (Reservation reservation : pendingReservations) {
            if (!reservation.getDateFrom().isAfter(LocalDate.now())) {
                reservation.setReservationStatus(EReservationStatus.DECLINED);

                reservationRepository.save(reservation);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void markPassedReservations() {
        List<Reservation> acceptedReservations = reservationRepository
                .findByReservationStatus(EReservationStatus.ACCEPTED);

        for (Reservation reservation : acceptedReservations) {
            if (reservation.getDateTo().isBefore(LocalDate.now())) {
                reservation.setReservationStatus(EReservationStatus.PASSED);

                reservationRepository.save(reservation);
            }
        }
    }
}
