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
import ru.practicum.main_service.event.mapper.EventMapper;
import ru.practicum.main_service.event.mapper.LocationMapper;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.event.repository.EventRepository;
import ru.practicum.main_service.event.repository.LocationRepository;
import ru.practicum.main_service.event.repository.RequestRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;
import ru.practicum.stats_common.model.ViewStats;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsService statsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Вывод событий на запрос администратора с параметрами users = {}, states = {}, categoriesId = {}, " +
                "rangeStart = {}, rangeEnd = {}, from = {}, size = {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        checkStartIsBeforeEnd(rangeStart, rangeEnd);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (users != null && !users.isEmpty()) {
            criteria = builder.and(criteria, root.get("initiator").in(users));
        }

        if (states != null && !states.isEmpty()) {
            criteria = builder.and(criteria, root.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").in(categories));
        }

        if (rangeStart != null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query).setFirstResult(from).setMaxResults(size).getResultList();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> views = getViews(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map((event) -> eventMapper.toEventFullDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Обновление события с id {} по запросу администратора с параметрами {}", eventId, updateEventAdminRequest);

        if (updateEventAdminRequest.getEventDate() != null &&
                updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "один час от текущего момента. Value: %s", updateEventAdminRequest.getEventDate()));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует."));

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateEventAdminRequest.getCategory()));
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            Location newLocation = locationMapper.toLocation(updateEventAdminRequest.getLocation());
            Location eventLocation = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                    .orElseGet(() -> locationRepository.save(newLocation));
            event.setLocation(eventLocation);
        }


        if (updateEventAdminRequest.getParticipantLimit() != null) {
            if ((updateEventAdminRequest.getParticipantLimit() == 0) ||
                    (updateEventAdminRequest.getParticipantLimit() >=
                            getConfirmedRequests(List.of(event)).getOrDefault(eventId, 0L))
            ) {
                event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
            } else {
                throw new ForbiddenException(String.format("Field: stateAction. Error: Новый лимит участников должен " +
                        "быть не меньше количества уже одобренных заявок: %s", event.getParticipantLimit()));
            }
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new ForbiddenException(String.format("Field: stateAction. Error: опубликовать можно только " +
                        "события, находящиеся в ожидании публикации. Текущий статус: %s", event.getState()));
            }

            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.REJECTED);
                    break;
            }
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        eventRepository.save(event);

        return getEventByPrivate(event.getInitiator().getId(), eventId);
    }

    @Override
    public List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable) {
        log.info("Вывод всех событий пользователя с id {} и пагинацией {}", userId, pageable);

        userService.getUserById(userId);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);

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

        Map<Long, Long> confirmedRequests = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

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
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        eventRepository.save(event);

        return getEventByPrivate(userId, eventId);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, EventSortType sort, Integer from, Integer size, HttpServletRequest request) {
        log.info("Вывод событий на публичный запрос с параметрами text = {}, categoriesId = {}, paid = {}, rangeStart = {}, " +
                "rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        checkStartIsBeforeEnd(rangeStart, rangeEnd);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null && !text.isBlank()) {
            Predicate annotation = builder.like(builder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate description = builder.like(builder.lower(root.get("description")), "%" + text.toLowerCase() + "%");
            criteria = builder.and(criteria, builder.or(annotation, description));
        }

        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").in(categories));
        }

        if (paid != null) {
            criteria = builder.and(criteria, root.get("paid").in(paid));
        }

        if (rangeStart == null && rangeEnd == null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        } else {
            if (rangeStart != null) {
                criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null) {
                criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
        }

        criteria = builder.and(criteria, root.get("state").in(EventState.PUBLISHED));

        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query).setFirstResult(from).setMaxResults(size).getResultList();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> (event.getParticipantLimit() == 0 ||
                            event.getParticipantLimit() > confirmedRequests.getOrDefault(event.getId(), 0L)))
                    .collect(Collectors.toList());

        }

        Map<Long, Long> views = getViews(events);

        List<EventShortDto> eventsShortDto = events.stream()
                .map((event) -> eventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());

        if (sort != null && sort.equals(EventSortType.VIEWS)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getViews));
        } else if (sort != null && sort.equals(EventSortType.EVENT_DATE)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        statsService.addHit(request);

        return eventsShortDto;
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId, HttpServletRequest request) {
        log.info("Вывод события с id {} на публичный запрос", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие с таким id не опубликовано.");
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        statsService.addHit(request);

        return eventMapper.toEventFullDto(event, confirmedRequests.getOrDefault(eventId, 0L),
                views.getOrDefault(eventId, 0L));
    }

    @Override
    public Event getEventById(Long eventId) {
        log.info("Вывод события с id {}", eventId);

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует."));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Event> publishedEvents = events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toList());

        List<Long> eventsId = publishedEvents.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> requestStats = new HashMap<>();

        requestRepository.getConfirmedRequests(eventsId)
                .forEach(stat -> requestStats.put(stat.getEventId(), stat.getConfirmedRequests()));

        return requestStats;
    }

    private Map<Long, Long> getViews(List<Event> events) {
        Map<Long, Long> views = new HashMap<>();

        Optional<LocalDateTime> minPublishedOn = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        if (minPublishedOn.isPresent()) {
            LocalDateTime start = minPublishedOn.get();
            LocalDateTime end = LocalDateTime.now();
            List<String> uris = events.stream()
                    .map(Event::getId)
                    .map(id -> ("/events/" + id))
                    .collect(Collectors.toList());

            List<ViewStats> stats = statsService.getStats(start, end, uris, null);
            stats.forEach(stat -> {
                Long eventId = Long.parseLong(stat.getUri()
                        .split("/", 0)[2]);
                views.put(eventId, views.getOrDefault(eventId, 0L) + stat.getHits());
            });
        }
        return views;
    }

    void checkStartIsBeforeEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: некорректные параметры временного " +
                    "интервала. Value: rangeStart = %s, rangeEnd = %s", rangeStart, rangeEnd));
        }
    }
}
