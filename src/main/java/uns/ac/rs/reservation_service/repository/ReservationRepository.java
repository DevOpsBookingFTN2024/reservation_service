package uns.ac.rs.reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uns.ac.rs.reservation_service.model.Reservation;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByGuest(String guest);
    List<Reservation> findByIdAccommodation(UUID idAccommodation);
}
