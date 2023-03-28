package ru.practicum.main_service.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.dto.UpdateEventAdminRequest;
import ru.practicum.main_service.event.dto.UpdateEventUserRequest;
import ru.practicum.main_service.event.enums.EventSortType;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable);

    EventFullDto createEventByPrivate(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByPrivate(Long userId, Long eventId);

    EventFullDto patchEventByPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getEventsByPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort,
                                          Integer from, Integer size, HttpServletRequest request);

    EventFullDto getEventByPublic(Long id, HttpServletRequest request);

    Event getEventById(Long eventId);

    List<Event> getEventsByIds(List<Long> eventsId);

    List<EventShortDto> toEventsShortDto(List<Event> events);
}
