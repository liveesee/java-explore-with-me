package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT pr.event.id AS eventId, COUNT(pr) AS count FROM ParticipationRequest pr "
            + "WHERE pr.event.id IN :eventIds AND pr.status = :status GROUP BY pr.event.id")
    List<EventConfirmedCount> countConfirmedByEventIds(@Param("eventIds") Collection<Long> eventIds,
                                                       @Param("status") RequestStatus status);

    @Query("SELECT pr FROM ParticipationRequest pr "
            + "WHERE pr.event.id = :eventId AND pr.status = :status")
    List<ParticipationRequest> findByEventIdAndStatus(@Param("eventId") Long eventId,
                                                      @Param("status") RequestStatus status);

    interface EventConfirmedCount {
        Long getEventId();

        Long getCount();
    }
}
