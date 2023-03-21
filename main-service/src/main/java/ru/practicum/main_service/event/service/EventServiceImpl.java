package ru.practicum.main_service.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.service.CategoryService;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.dto.UpdateEventAdminRequest;
import ru.practicum.main_service.event.dto.UpdateEventUserRequest;
import ru.practicum.main_service.event.enums.EventSortType;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.enums.EventStateAction;
import ru.practicum.main_service.event.mapper.EventMapper;
import ru.practicum.main_service.event.mapper.LocationMapper;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.event.repository.EventRepository;
import ru.practicum.main_service.event.repository.LocationRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;
import ru.practicum.stats_common.model.ViewStats;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsService statsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        log.info("Вывод событий на запрос администратора с параметрами users = {}, states = {}, categoriesId = {}, " +
                "rangeStart = {}, rangeEnd = {}, pageable = {}", users, states, categories, rangeStart, rangeEnd, pageable);
        //TODO



        return null;
    }

    @Override
    @Transactional
    public EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Обновление события с id {} по запросу администратора с параметрами {}", eventId, updateEventAdminRequest);
        //TODO




        return null;
    }

    @Override
    public List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable) {
        log.info("Вывод всех событий пользователя с id {} и пагинацией {}", userId, pageable);

        userService.getUserById(userId);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> confirmedRequests = new HashMap<>();
        Map<Long, Long> views = new HashMap<>();

        Optional<LocalDateTime> optionalStart = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        if (optionalStart.isPresent()) {
            //TODO выборка requests
            // requests = requestService.getCountRequestsByListEvents(events);

            LocalDateTime start = optionalStart.get();
            LocalDateTime end = LocalDateTime.now();
            List<String> uris = events.stream()
                    .map(Event::getId)
                    .map(id -> ("/event/" + id))
                    .collect(Collectors.toList());

            List<ViewStats> stats = statsService.getStats(start, end, uris, null);
            stats.forEach(stat -> {
                Long eventId = Long.parseLong(stat.getUri()
                        .split("/", 0)[2]);
                views.put(eventId, views.getOrDefault(eventId, 0L) + stat.getHits());
            });
        }

        return events.stream()
                .map((event) -> eventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEventByPrivate(Long userId, NewEventDto newEventDto) {
        log.info("Создание нового события пользователем с id {} и параметрами {}", userId, newEventDto);

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "два часа от текущего момента. Value: %s", newEventDto.getEventDate()));
        }

        User eventUser = userService.getUserById(userId);
        Category eventCategory = categoryService.getCategoryById(newEventDto.getCategory());

        Location newLocation = locationMapper.toLocation(newEventDto.getLocation());
        Location eventLocation = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElseGet(() -> locationRepository.save(newLocation));

        Event newEvent = eventMapper.toEvent(newEventDto, eventUser, eventCategory, eventLocation, LocalDateTime.now(),
                EventState.PENDING);

        return eventMapper.toEventFullDto(eventRepository.save(newEvent), 0L, 0L);
    }

    @Override
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        log.info("Вывод события с id {}, созданного пользователем с id {}", eventId, userId);

        userService.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует."));

        Map<Long, Long> confirmedRequests = new HashMap<>();
        Map<Long, Long> views = new HashMap<>();

        LocalDateTime start = event.getCreatedOn();
        if (start != null) {
            //TODO выборка requests
            // requests = requestService.getCountRequestsByListEvents(events);

            List<String> uris = List.of("/event/" + event.getId());
            List<ViewStats> stats = statsService.getStats(start, start, uris, null);
            stats.forEach(stat -> views.put(eventId, views.getOrDefault(eventId, 0L) + stat.getHits()));
        }

        return eventMapper.toEventFullDto(event, confirmedRequests.getOrDefault(eventId, 0L),
                views.getOrDefault(eventId, 0L));
    }

    @Override
    @Transactional
    public EventFullDto patchEventByPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Обновление события с id {} по запросу пользователя с id {} с новыми параметрами {}",
                eventId, userId, updateEventUserRequest);

        if (updateEventUserRequest.getEventDate() != null &&
                updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "два часа от текущего момента. Value: %s", updateEventUserRequest.getEventDate()));
        }

        userService.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует."));

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("Изменять можно только неопубликованные или отмененные события.");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateEventUserRequest.getCategory()));
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location newLocation = locationMapper.toLocation(updateEventUserRequest.getLocation());
            Location eventLocation = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                    .orElseGet(() -> locationRepository.save(newLocation));
            event.setLocation(eventLocation);
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        eventRepository.save(event);

        return getEventByPrivate(userId, eventId);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(String text, List<Long> categories, Boolean paid,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                 EventSortType sort, Pageable pageable) {
        log.info("Вывод событий на публичный запрос с параметрами text = {}, categoriesId = {}, paid = {}, rangeStart = {}, " +
                "rangeEnd = {}, onlyAvailable = {}, sort = {}, pageable = {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageable);
        //TODO




        return null;
    }

    @Override
    public EventFullDto getEventByPublic(Long id) {
        log.info("Вывод события с id {} на публичный запрос", id);
        //TODO




        return null;
    }
}
