package ru.practicum.main_service.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.category.dto.CategoryDto;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.service.CategoryService;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.dto.UpdateEventAdminRequest;
import ru.practicum.main_service.event.dto.UpdateEventUserRequest;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.enums.EventStateAction;
import ru.practicum.main_service.event.mapper.EventMapperImpl;
import ru.practicum.main_service.event.mapper.LocationMapperImpl;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.event.repository.EventRepository;
import ru.practicum.main_service.event.repository.LocationRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.dto.UserShortDto;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private StatsService statsService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LocationMapperImpl locationMapper;

    @Mock
    private EventMapperImpl eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final User user = User.builder()
            .id(1L)
            .name("test user")
            .email("test@yandex.ru")
            .build();
    private final UserShortDto userShortDto = UserShortDto.builder()
            .id(user.getId())
            .name(user.getName())
            .build();
    private final Category category = Category.builder()
            .id(1L)
            .name("test category")
            .build();
    private final CategoryDto categoryDto = CategoryDto.builder()
            .id(1L)
            .name("test category")
            .build();
    private final Category updatedCategory = Category.builder()
            .id(2L)
            .name("updated test category")
            .build();
    private final Location location = Location.builder()
            .id(1L)
            .lat(11.1524F)
            .lon(-5.0010F)
            .build();
    private final LocationDto locationDto = LocationDto.builder()
            .lat(location.getLat())
            .lon(location.getLon())
            .build();
    private final Location updatedLocation = Location.builder()
            .id(2L)
            .lat(0F)
            .lon(0F)
            .build();
    private final LocationDto updatedLocationDto = LocationDto.builder()
            .lat(updatedLocation.getLat())
            .lon(updatedLocation.getLon())
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .title("test title 1")
            .annotation("test annotation 1")
            .description("test description 1")
            .eventDate(LocalDateTime.now().plusDays(2))
            .category(category)
            .location(location)
            .paid(false)
            .participantLimit(0)
            .requestModeration(false)
            .initiator(user)
            .state(EventState.PENDING)
            .createdOn(LocalDateTime.now().minusHours(3))
            .publishedOn(null)
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .title("test title 2")
            .annotation("test annotation 2")
            .description("test description 2")
            .eventDate(LocalDateTime.now().plusDays(4))
            .category(category)
            .location(location)
            .paid(true)
            .participantLimit(50)
            .requestModeration(true)
            .initiator(user)
            .state(EventState.CANCELED)
            .createdOn(LocalDateTime.now().minusDays(1))
            .publishedOn(LocalDateTime.now().minusHours(3))
            .build();
    private final Event event3 = Event.builder()
            .id(3L)
            .eventDate(LocalDateTime.now().plusDays(4))
            .initiator(user)
            .state(EventState.PUBLISHED)
            .build();
    private final EventFullDto eventFullDto1 = EventFullDto.builder()
            .id(event1.getId())
            .title(event1.getTitle())
            .annotation(event1.getAnnotation())
            .description(event1.getDescription())
            .eventDate(event1.getEventDate())
            .category(categoryDto)
            .location(locationDto)
            .paid(event1.getPaid())
            .participantLimit(event1.getParticipantLimit())
            .requestModeration(event1.getRequestModeration())
            .initiator(userShortDto)
            .state(event1.getState())
            .createdOn(event1.getCreatedOn())
            .publishedOn(event1.getPublishedOn())
            .views(60L)
            .confirmedRequests(10L)
            .build();
    private final EventShortDto eventShortDto1 = EventShortDto.builder()
            .id(event1.getId())
            .title(event1.getTitle())
            .annotation(event1.getAnnotation())
            .eventDate(event1.getEventDate())
            .category(categoryDto)
            .paid(event1.getPaid())
            .initiator(userShortDto)
            .views(60L)
            .confirmedRequests(10L)
            .build();
    private Map<Long, Long> confirmedRequests;
    private Map<Long, Long> views;
    private UpdateEventAdminRequest updateEventAdminRequest;
    private Event updatedEvent1;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateEventUserRequest;

    @BeforeEach
    public void beforeEach() {
        confirmedRequests = new HashMap<>();
        confirmedRequests.put(event1.getId(), eventShortDto1.getConfirmedRequests());
        views = new HashMap<>();
        views.put(event1.getId(), eventShortDto1.getViews());
    }

    @Nested
    class PatchEventByAdmin {
        @BeforeEach
        public void beforeEach() {
            updateEventAdminRequest = UpdateEventAdminRequest.builder()
                    .title("updated test title 1")
                    .annotation("updated test annotation 1")
                    .description("updated test description 1")
                    .eventDate(LocalDateTime.now().plusDays(5))
                    .category(updatedCategory.getId())
                    .location(updatedLocationDto)
                    .paid(true)
                    .participantLimit(50)
                    .requestModeration(true)
                    .stateAction(EventStateAction.PUBLISH_EVENT)
                    .build();
            updatedEvent1 = Event.builder()
                    .id(1L)
                    .title(updateEventAdminRequest.getTitle())
                    .annotation(updateEventAdminRequest.getAnnotation())
                    .description(updateEventAdminRequest.getDescription())
                    .eventDate(updateEventAdminRequest.getEventDate())
                    .category(updatedCategory)
                    .location(updatedLocation)
                    .paid(updateEventAdminRequest.getPaid())
                    .participantLimit(updateEventAdminRequest.getParticipantLimit())
                    .requestModeration(updateEventAdminRequest.getRequestModeration())
                    .initiator(event1.getInitiator())
                    .state(EventState.PUBLISHED)
                    .createdOn(event1.getCreatedOn())
                    .publishedOn(LocalDateTime.now())
                    .build();
            views.put(event1.getId(), eventFullDto1.getViews());
        }

        @Test
        public void shouldPublished() {
            confirmedRequests.put(event1.getId(), eventFullDto1.getConfirmedRequests());

            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updatedCategory.getId())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(updatedEvent1));
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest);

            assertEquals(eventFullDto1, eventFullDto);

            verify(eventRepository, times(1)).findById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(statsService, times(2)).getConfirmedRequests(any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(updatedEvent1, savedEvent);
        }

        @Test
        public void shouldRejected() {
            confirmedRequests.put(event1.getId(), eventFullDto1.getConfirmedRequests());
            updateEventAdminRequest.setStateAction(EventStateAction.REJECT_EVENT);
            updatedEvent1.setState(EventState.REJECTED);

            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updatedCategory.getId())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(updatedEvent1));
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest);

            assertEquals(eventFullDto1, eventFullDto);

            verify(eventRepository, times(1)).findById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(statsService, times(2)).getConfirmedRequests(any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(updatedEvent1, savedEvent);
        }

        @Test
        public void shouldPublishedIfNewParticipantLimitIsZero() {
            confirmedRequests.put(event1.getId(), eventFullDto1.getConfirmedRequests());
            updateEventAdminRequest.setParticipantLimit(0);
            updatedEvent1.setParticipantLimit(0);

            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updatedCategory.getId())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(updatedEvent1));
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest);

            assertEquals(eventFullDto1, eventFullDto);

            verify(eventRepository, times(1)).findById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(updatedEvent1, savedEvent);
        }

        @Test
        public void shouldThrowExceptionIfNewEventDateNotValid() {
            updateEventAdminRequest.setEventDate(LocalDateTime.now());

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest));
            assertEquals(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "один час от текущего момента. Value: %s", updateEventAdminRequest.getEventDate()), exception.getMessage());

            verify(eventRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(eventRepository.findById(event1.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest));
            assertEquals("События с таким id не существует.", exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
            verify(eventRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfNewParticipantLimit() {
            updateEventAdminRequest.setParticipantLimit(50);
            confirmedRequests.put(event1.getId(), 51L);

            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updatedCategory.getId())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.patchEventByAdmin(event1.getId(), updateEventAdminRequest));
            assertEquals(String.format("Field: stateAction. Error: Новый лимит участников должен " +
                    "быть не меньше количества уже одобренных заявок: %s", confirmedRequests.get(event1.getId())),
                    exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(eventRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfEventStateNotPending() {
            when(eventRepository.findById(event2.getId())).thenReturn(Optional.of(event2));
            when(categoryService.getCategoryById(updatedCategory.getId())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.patchEventByAdmin(event2.getId(), updateEventAdminRequest));
            assertEquals(String.format("Field: stateAction. Error: опубликовать можно только " +
                            "события, находящиеся в ожидании публикации. Текущий статус: %s", event2.getState()),
                    exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class GetAllEventsByPrivate {
        @Test
        public void shouldGet() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findAllByInitiatorId(event1.getInitiator().getId(), pageable)).thenReturn(List.of(event1));
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventShortDto(any(), any(), any())).thenReturn(eventShortDto1);

            List<EventShortDto> eventsShortDto = eventService.getAllEventsByPrivate(event1.getInitiator().getId(), pageable);

            assertEquals(1, eventsShortDto.size());
            assertEquals(eventShortDto1, eventsShortDto.get(0));

            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findAllByInitiatorId(any(), any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventShortDto(any(), any(), any());
        }
    }

    @Nested
    class CreateEventByPrivate {
        @BeforeEach
        public void beforeEach() {
            newEventDto = NewEventDto.builder()
                    .title(event1.getTitle())
                    .annotation(event1.getAnnotation())
                    .description(event1.getDescription())
                    .eventDate(event1.getEventDate())
                    .category(event1.getCategory().getId())
                    .location(locationDto)
                    .paid(event1.getPaid())
                    .participantLimit(event1.getParticipantLimit())
                    .requestModeration(event1.getRequestModeration())
                    .build();
        }

        @Test
        public void shouldCreate() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(categoryService.getCategoryById(newEventDto.getCategory())).thenReturn(category);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(location);
            when(eventMapper.toEvent(any(), any(), any(), any(), any(), any())).thenReturn(event1);
            when(eventRepository.save(any())).thenReturn(event1);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDtoFromRepository = eventService.createEventByPrivate(event1.getInitiator().getId(), newEventDto);

            assertEquals(eventFullDto1, eventFullDtoFromRepository);

            verify(userService, times(1)).getUserById(any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(eventMapper, times(1)).toEvent(any(), any(), any(), any(), any(), any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(event1, savedEvent);
        }

        @Test
        public void shouldThrowExceptionIfEventDateNotValid() {
            newEventDto.setEventDate(LocalDateTime.now());

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.createEventByPrivate(event1.getInitiator().getId(), newEventDto));
            assertEquals(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "два часа от текущего момента. Value: %s", newEventDto.getEventDate()), exception.getMessage());

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class GetEventByPrivate {
        @Test
        public void shouldGet() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(event1));
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDtoFromRepository = eventService.getEventByPrivate(event1.getInitiator().getId(),
                    event1.getId());

            assertEquals(eventFullDto1, eventFullDtoFromRepository);

            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.getEventByPrivate(event1.getInitiator().getId(), event1.getId()));
            assertEquals("События с таким id не существует.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
        }
    }

    @Nested
    class PatchEventByPrivate {
        @BeforeEach
        public void beforeEach() {
            updateEventUserRequest = UpdateEventUserRequest.builder()
                    .title("updated test title 1")
                    .annotation("updated test annotation 1")
                    .description("updated test description 1")
                    .eventDate(LocalDateTime.now().plusDays(5))
                    .category(updatedCategory.getId())
                    .location(updatedLocationDto)
                    .paid(true)
                    .participantLimit(50)
                    .requestModeration(true)
                    .stateAction(EventStateAction.SEND_TO_REVIEW)
                    .build();
            updatedEvent1 = Event.builder()
                    .id(1L)
                    .title(updateEventUserRequest.getTitle())
                    .annotation(updateEventUserRequest.getAnnotation())
                    .description(updateEventUserRequest.getDescription())
                    .eventDate(updateEventUserRequest.getEventDate())
                    .category(updatedCategory)
                    .location(updatedLocation)
                    .paid(updateEventUserRequest.getPaid())
                    .participantLimit(updateEventUserRequest.getParticipantLimit())
                    .requestModeration(updateEventUserRequest.getRequestModeration())
                    .initiator(event1.getInitiator())
                    .state(EventState.PENDING)
                    .createdOn(event1.getCreatedOn())
                    .publishedOn(event1.getPublishedOn())
                    .build();
        }

        @Test
        public void shouldSendToReview() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updateEventUserRequest.getCategory())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(eventRepository.save(any())).thenReturn(updatedEvent1);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.patchEventByPrivate(event1.getInitiator().getId(), event1.getId(),
                    updateEventUserRequest);

            assertEquals(eventFullDto1, eventFullDto);

            verify(userService, times(2)).getUserById(any());
            verify(eventRepository, times(2)).findByIdAndInitiatorId(any(), any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(updatedEvent1, savedEvent);
        }

        @Test
        public void shouldCanceled() {
            updateEventUserRequest.setStateAction(EventStateAction.CANCEL_REVIEW);
            updatedEvent1.setState(EventState.CANCELED);

            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.of(event1));
            when(categoryService.getCategoryById(updateEventUserRequest.getCategory())).thenReturn(updatedCategory);
            when(locationMapper.toLocation(any())).thenCallRealMethod();
            when(locationRepository.findByLatAndLon(updatedLocationDto.getLat(), updatedLocationDto.getLon()))
                    .thenReturn(Optional.empty());
            when(locationRepository.save(any())).thenReturn(updatedLocation);
            when(eventRepository.save(any())).thenReturn(updatedEvent1);
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.patchEventByPrivate(event1.getInitiator().getId(), event1.getId(),
                    updateEventUserRequest);

            assertEquals(eventFullDto1, eventFullDto);

            verify(userService, times(2)).getUserById(any());
            verify(eventRepository, times(2)).findByIdAndInitiatorId(any(), any());
            verify(categoryService, times(1)).getCategoryById(any());
            verify(locationMapper, times(1)).toLocation(any());
            verify(locationRepository, times(1)).findByLatAndLon(any(), any());
            verify(locationRepository, times(1)).save(any());
            verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(statsService, times(1)).getViews(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());

            Event savedEvent = eventArgumentCaptor.getValue();

            checkResults(updatedEvent1, savedEvent);
        }

        @Test
        public void shouldThrowExceptionIfNewEventDateNotValid() {
            updateEventUserRequest.setEventDate(LocalDateTime.now());

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.patchEventByPrivate(event1.getInitiator().getId(), event1.getId(),
                            updateEventUserRequest));
            assertEquals(String.format("Field: eventDate. Error: не может быть ранее, чем через " +
                    "два часа от текущего момента. Value: %s", updateEventUserRequest.getEventDate()), exception.getMessage());
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(userService.getUserById(event1.getInitiator().getId())).thenReturn(event1.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event1.getId(), event1.getInitiator().getId()))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.patchEventByPrivate(event1.getInitiator().getId(), event1.getId(),
                            updateEventUserRequest));
            assertEquals("События с таким id не существует.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
        }

        @Test
        public void shouldThrowExceptionIfEventIsPublished() {
            when(userService.getUserById(event3.getInitiator().getId())).thenReturn(event3.getInitiator());
            when(eventRepository.findByIdAndInitiatorId(event3.getId(), event3.getInitiator().getId()))
                    .thenReturn(Optional.of(event3));

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> eventService.patchEventByPrivate(event3.getInitiator().getId(), event3.getId(),
                            updateEventUserRequest));
            assertEquals("Изменять можно только неопубликованные или отмененные события.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventRepository, times(1)).findByIdAndInitiatorId(any(), any());
        }
    }

    @Nested
    class GetEventByPublic {
        @Test
        public void shouldGet() {
            when(eventRepository.findById(event3.getId())).thenReturn(Optional.of(event3));
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(statsService.getViews(any())).thenReturn(views);
            when(eventMapper.toEventFullDto(any(), any(), any())).thenReturn(eventFullDto1);

            EventFullDto eventFullDto = eventService.getEventByPublic(event3.getId(), new MockHttpServletRequest());

            assertEquals(eventFullDto1, eventFullDto);

            verify(eventRepository, times(1)).findById(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(statsService, times(1)).getViews(any());
            verify(statsService, times(1)).addHit(any());
            verify(eventMapper, times(1)).toEventFullDto(any(), any(), any());
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(eventRepository.findById(event3.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.getEventByPublic(event3.getId(), new MockHttpServletRequest()));
            assertEquals("События с таким id не существует.", exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
        }

        @Test
        public void shouldThrowExceptionIfEventNotPublished() {
            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.getEventByPublic(event1.getId(), new MockHttpServletRequest()));
            assertEquals("Событие с таким id не опубликовано.", exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
        }
    }

    @Nested
    class GetEventById {
        @Test
        public void shouldGet() {
            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));

            Event result = eventService.getEventById(event1.getId());

            assertEquals(event1, result);

            verify(eventRepository, times(1)).findById(any());
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(eventRepository.findById(event1.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> eventService.getEventById(event1.getId()));
            assertEquals("События с таким id не существует.", exception.getMessage());

            verify(eventRepository, times(1)).findById(any());
        }
    }

    @Nested
    class GetEventsByIds {
        @Test
        public void shouldGet() {
            when(eventRepository.findAllByIdIn(List.of(event1.getId(), event2.getId()))).thenReturn(List.of(event1, event2));

            List<Event> result = eventService.getEventsByIds(List.of(event1.getId(), event2.getId()));

            assertEquals(2, result.size());

            Event resultEvent1 = result.get(0);
            Event resultEvent2 = result.get(1);

            assertEquals(event1, resultEvent1);
            assertEquals(event2, resultEvent2);

            verify(eventRepository, times(1)).findAllByIdIn(any());
        }

        @Test
        public void shouldGetEmpty() {
            when(eventRepository.findAllByIdIn(List.of(event1.getId(), event2.getId()))).thenReturn(List.of());

            List<Event> result = eventService.getEventsByIds(List.of(event1.getId(), event2.getId()));

            assertTrue(result.isEmpty());

            verify(eventRepository, times(1)).findAllByIdIn(any());
        }
    }

    private void checkResults(Event event, Event result) {
        assertEquals(event.getId(), result.getId());
        assertEquals(event.getAnnotation(), result.getAnnotation());
        assertEquals(event.getDescription(), result.getDescription());
        assertEquals(event.getCategory(), result.getCategory());
        assertEquals(event.getEventDate(), result.getEventDate());
        assertEquals(event.getPaid(), result.getPaid());
        assertEquals(event.getLocation(), result.getLocation());
        assertEquals(event.getParticipantLimit(), result.getParticipantLimit());
        assertEquals(event.getRequestModeration(), result.getRequestModeration());
        assertEquals(event.getState(), result.getState());
        assertEquals(event.getTitle(), result.getTitle());
    }
}
