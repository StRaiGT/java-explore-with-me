package ru.practicum.main_service.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_service.event.dto.RequestStats;
import ru.practicum.main_service.event.enums.RequestStatus;
import ru.practicum.main_service.event.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    @Query("SELECT new ru.practicum.main_service.event.dto.RequestStats(r.event.id, count(r.id)) " +
            "FROM Request AS r " +
            "WHERE r.event.id IN ?1 " +
            "AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<RequestStats> getConfirmedRequests(List<Long> eventsId);
}
