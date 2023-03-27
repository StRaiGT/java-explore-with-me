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
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main_service.event.dto.ParticipationRequestDto;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.enums.RequestStatus;
import ru.practicum.main_service.event.enums.RequestStatusAction;
import ru.practicum.main_service.event.mapper.RequestMapperImpl;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Request;
import ru.practicum.main_service.event.repository.RequestRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private StatsService statsService;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestMapperImpl requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Captor
    private ArgumentCaptor<Request> requestArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<Request>> requestsArgumentCaptor;

    private final User user1 = User.builder()
            .id(1L)
            .name("test user1")
            .email("test1@yandex.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("test user2")
            .email("test2@yandex.ru")
            .build();
    private final User user3 = User.builder()
            .id(3L)
            .name("test user3")
            .email("test3@yandex.ru")
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .eventDate(LocalDateTime.now().plusDays(2))
            .participantLimit(0)
            .requestModeration(false)
            .initiator(user1)
            .state(EventState.PENDING)
            .createdOn(LocalDateTime.now().minusHours(7))
            .publishedOn(LocalDateTime.now().minusHours(4))
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .eventDate(LocalDateTime.now().plusDays(4))
            .participantLimit(2)
            .requestModeration(true)
            .initiator(user1)
            .state(EventState.PUBLISHED)
            .createdOn(LocalDateTime.now().minusDays(1))
            .publishedOn(LocalDateTime.now().minusHours(2))
            .build();
    private final Event event3 = Event.builder()
            .id(3L)
            .eventDate(LocalDateTime.now().plusDays(8))
            .participantLimit(500)
            .requestModeration(false)
            .initiator(user1)
            .state(EventState.PUBLISHED)
            .createdOn(LocalDateTime.now().minusDays(5))
            .publishedOn(LocalDateTime.now().minusHours(2))
            .build();
    private final Request request1 = Request.builder()
            .id(1L)
            .event(event1)
            .requester(user2)
            .created(event1.getPublishedOn().plusHours(1))
            .status(RequestStatus.CONFIRMED)
            .build();
    private final Request request2 = Request.builder()
            .id(2L)
            .event(event3)
            .requester(user2)
            .created(event3.getPublishedOn().plusHours(2))
            .status(RequestStatus.CONFIRMED)
            .build();
    private final Request request3 = Request.builder()
            .id(3L)
            .event(event1)
            .requester(user3)
            .created(event1.getPublishedOn().plusHours(3))
            .status(RequestStatus.PENDING)
            .build();
    private final Request request4 = Request.builder()
            .id(4L)
            .event(event2)
            .requester(user2)
            .created(event2.getPublishedOn().plusHours(1))
            .status(RequestStatus.PENDING)
            .build();
    private final ParticipationRequestDto participationRequestDto1 = ParticipationRequestDto.builder()
            .id(request1.getId())
            .event(request1.getEvent().getId())
            .requester(request1.getRequester().getId())
            .created(request1.getCreated())
            .status(request1.getStatus())
            .build();
    private final ParticipationRequestDto participationRequestDto2 = ParticipationRequestDto.builder()
            .id(request2.getId())
            .event(request2.getEvent().getId())
            .requester(request2.getRequester().getId())
            .created(request2.getCreated())
            .status(request2.getStatus())
            .build();
    private final ParticipationRequestDto participationRequestDto4 = ParticipationRequestDto.builder()
            .id(request4.getId())
            .event(request4.getEvent().getId())
            .requester(request4.getRequester().getId())
            .created(request4.getCreated())
            .status(request4.getStatus())
            .build();
    private Map<Long, Long> confirmedRequests;
    private EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest;

    @Nested
    class GetEventRequestsByRequester {
        @Test
        public void shouldGet() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(requestRepository.findAllByRequesterId(user2.getId())).thenReturn(List.of(request1, request4));
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            List<ParticipationRequestDto> participationRequestsDto = requestService.getEventRequestsByRequester(user2.getId());

            assertEquals(2, participationRequestsDto.size());

            ParticipationRequestDto participationFromRepository1 = participationRequestsDto.get(0);
            ParticipationRequestDto participationFromRepository2 = participationRequestsDto.get(1);

            assertEquals(participationRequestDto1, participationFromRepository1);
            assertEquals(participationRequestDto4, participationFromRepository2);

            verify(userService, times(1)).getUserById(any());
            verify(requestRepository, times(1)).findAllByRequesterId(any());
            verify(requestMapper, times(2)).toParticipationRequestDto(any());
        }

        @Test
        public void shouldGetEmpty() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(requestRepository.findAllByRequesterId(user1.getId())).thenReturn(List.of());

            List<ParticipationRequestDto> participationRequestsDto = requestService.getEventRequestsByRequester(user1.getId());

            assertTrue(participationRequestsDto.isEmpty());

            verify(userService, times(1)).getUserById(any());
            verify(requestRepository, times(1)).findAllByRequesterId(any());
            verify(requestMapper, times(0)).toParticipationRequestDto(any());
        }
    }

    @Nested
    class CreateEventRequest {
        @BeforeEach
        public void beforeEach() {
            confirmedRequests = new HashMap<>();
            confirmedRequests.put(2L, 2L);
        }

        @Test
        public void shouldCreatePending() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findByEventIdAndRequesterId(event2.getId(), user2.getId())).thenReturn(Optional.empty());
            when(statsService.getConfirmedRequests(List.of(event2))).thenReturn(new HashMap<>());
            when(requestRepository.save(any())).thenReturn(request4);
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            ParticipationRequestDto participationRequestDto = requestService.createEventRequest(user2.getId(), event2.getId());

            assertEquals(participationRequestDto4, participationRequestDto);

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findByEventIdAndRequesterId(any(), any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(requestRepository, times(1)).save(requestArgumentCaptor.capture());
            verify(requestMapper, times(1)).toParticipationRequestDto(any());

            Request savedRequest = requestArgumentCaptor.getValue();

            assertNull(savedRequest.getId());
            assertEquals(event2.getId(), savedRequest.getEvent().getId());
            assertEquals(user2.getId(), savedRequest.getRequester().getId());
            assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
            assertNotNull(savedRequest.getCreated());
        }

        @Test
        public void shouldCreateConfirmed() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event3.getId())).thenReturn(event3);
            when(requestRepository.findByEventIdAndRequesterId(event3.getId(), user2.getId())).thenReturn(Optional.empty());
            when(statsService.getConfirmedRequests(List.of(event3))).thenReturn(new HashMap<>());
            when(requestRepository.save(any())).thenReturn(request2);
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            ParticipationRequestDto participationRequestDto = requestService.createEventRequest(user2.getId(), event3.getId());

            assertEquals(participationRequestDto2, participationRequestDto);

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findByEventIdAndRequesterId(any(), any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(requestRepository, times(1)).save(requestArgumentCaptor.capture());
            verify(requestMapper, times(1)).toParticipationRequestDto(any());

            Request savedRequest = requestArgumentCaptor.getValue();

            assertNull(savedRequest.getId());
            assertEquals(event3.getId(), savedRequest.getEvent().getId());
            assertEquals(user2.getId(), savedRequest.getRequester().getId());
            assertEquals(RequestStatus.CONFIRMED, savedRequest.getStatus());
            assertNotNull(savedRequest.getCreated());
        }

        @Test
        public void shouldTrowExceptionIfEventOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.createEventRequest(user1.getId(), event2.getId()));
            assertEquals("Нельзя создавать запрос на собственное событие.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, never()).save(any());
        }

        @Test
        public void shouldTrowExceptionIfEventNotPublished() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event1.getId())).thenReturn(event1);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.createEventRequest(user2.getId(), event1.getId()));
            assertEquals("Нельзя создавать запрос на неопубликованное событие.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, never()).save(any());
        }

        @Test
        public void shouldTrowExceptionIfRequestedTwice() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findByEventIdAndRequesterId(event2.getId(), user2.getId()))
                    .thenReturn(Optional.ofNullable(request4));

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.createEventRequest(user2.getId(), event2.getId()));
            assertEquals("Создавать повторный запрос запрещено.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findByEventIdAndRequesterId(any(), any());
            verify(requestRepository, never()).save(any());
        }

        @Test
        public void shouldTrowExceptionIfLimitReached() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findByEventIdAndRequesterId(event2.getId(), user2.getId())).thenReturn(Optional.empty());
            when(statsService.getConfirmedRequests(List.of(event2))).thenReturn(confirmedRequests);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.createEventRequest(user2.getId(), event2.getId()));
            assertEquals(String.format("Достигнут лимит подтвержденных запросов на участие: %s",
                    event2.getParticipantLimit()), exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findByEventIdAndRequesterId(any(), any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(requestRepository, never()).save(any());
        }
    }

    @Nested
    class CancelEventRequest {
        @Test
        public void shouldCancel() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(requestRepository.findById(request4.getId())).thenReturn(Optional.of(request4));
            when(requestRepository.save(any())).thenReturn(request4);
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            ParticipationRequestDto participationRequestDto = requestService.cancelEventRequest(user2.getId(), request4.getId());

            assertEquals(participationRequestDto4.getId(), participationRequestDto.getId());
            assertEquals(participationRequestDto4.getEvent(), participationRequestDto.getEvent());
            assertEquals(participationRequestDto4.getRequester(), participationRequestDto.getRequester());
            assertEquals(RequestStatus.CANCELED, participationRequestDto.getStatus());
            assertEquals(participationRequestDto4.getCreated(), participationRequestDto.getCreated());

            verify(userService, times(1)).getUserById(any());
            verify(requestRepository, times(1)).findById(any());
            verify(requestRepository, times(1)).save(requestArgumentCaptor.capture());
            verify(requestMapper, times(1)).toParticipationRequestDto(any());

            Request savedRequest = requestArgumentCaptor.getValue();

            assertEquals(request4.getId(), savedRequest.getId());
            assertEquals(request4.getEvent().getId(), savedRequest.getEvent().getId());
            assertEquals(request4.getRequester(), savedRequest.getRequester());
            assertEquals(RequestStatus.CANCELED, savedRequest.getStatus());
            assertEquals(request4.getCreated(), savedRequest.getCreated());
        }

        @Test
        public void shouldThrowExceptionIfRequestNotFound() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(requestRepository.findById(request4.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> requestService.cancelEventRequest(user2.getId(), request4.getId()));
            assertEquals("Заявки на участие с таким id не существует.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(requestRepository, times(1)).findById(any());
            verify(requestRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfUserNotRequestOwner() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(requestRepository.findById(request3.getId())).thenReturn(Optional.of(request3));

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.cancelEventRequest(user2.getId(), request3.getId()));
            assertEquals("Пользователь не является владельцем.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(requestRepository, times(1)).findById(any());
            verify(requestRepository, never()).save(any());
        }
    }

    @Nested
    class GetEventRequestsByEventOwner {
        @Test
        public void shouldGet() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event1.getId())).thenReturn(event1);
            when(requestRepository.findAllByEventId(event1.getId())).thenReturn(List.of(request1, request3));
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            List<ParticipationRequestDto> participationRequestsDto = requestService.getEventRequestsByEventOwner(user1.getId(), event1.getId());

            assertEquals(2, participationRequestsDto.size());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByEventId(any());
            verify(requestMapper, times(2)).toParticipationRequestDto(any());
        }

        @Test
        public void shouldGetEmpty() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event1.getId())).thenReturn(event1);
            when(requestRepository.findAllByEventId(event1.getId())).thenReturn(List.of());

            List<ParticipationRequestDto> participationRequestsDto = requestService.getEventRequestsByEventOwner(user1.getId(), event1.getId());

            assertTrue(participationRequestsDto.isEmpty());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByEventId(any());
        }

        @Test
        public void shouldTrowExceptionIfUserNotEventOwner() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event1.getId())).thenReturn(event1);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.getEventRequestsByEventOwner(user2.getId(), event1.getId()));
            assertEquals("Пользователь не является владельцем.", exception.getMessage());


            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
        }
    }

    @Nested
    class PatchEventRequestsByEventOwner {
        @BeforeEach
        public void beforeEach() {
            eventRequestStatusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                    .status(RequestStatusAction.REJECTED)
                    .requestIds(List.of(request4.getId()))
                    .build();
            confirmedRequests = new HashMap<>();
        }

        @Test
        public void shouldRejected() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds()))
                    .thenReturn(List.of(request4));
            when(requestRepository.saveAll(any())).thenReturn(List.of(request4));
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            EventRequestStatusUpdateResult result = requestService.patchEventRequestsByEventOwner(user1.getId(),
                    event2.getId(), eventRequestStatusUpdateRequest);

            assertEquals(1, result.getRejectedRequests().size());

            ParticipationRequestDto participationRequestDto = result.getRejectedRequests().get(0);

            assertEquals(request4.getId(), participationRequestDto.getId());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByIdIn(any());
            verify(requestRepository, times(1)).saveAll(requestsArgumentCaptor.capture());
            verify(requestMapper, times(1)).toParticipationRequestDto(any());

            List<Request> savedRequests = requestsArgumentCaptor.getValue();

            assertEquals(1, savedRequests.size());

            Request savedRequest1 = savedRequests.get(0);

            assertEquals(request4.getId(), savedRequest1.getId());
            assertEquals(request4.getRequester(), savedRequest1.getRequester());
            assertEquals(request4.getEvent().getId(), savedRequest1.getEvent().getId());
            assertEquals(request4.getCreated(), savedRequest1.getCreated());
            assertEquals(RequestStatus.REJECTED, savedRequest1.getStatus());
        }

        @Test
        public void shouldConfirmed() {
            confirmedRequests.put(event2.getId(), 1L);
            eventRequestStatusUpdateRequest.setStatus(RequestStatusAction.CONFIRMED);

            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds()))
                    .thenReturn(List.of(request4));
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);
            when(requestRepository.saveAll(List.of(request4))).thenReturn(List.of(request4));
            when(requestRepository.findAllByEventIdAndStatus(event2.getId(), RequestStatus.PENDING))
                    .thenReturn(List.of(request3));
            when(requestRepository.saveAll(List.of(request3))).thenReturn(List.of(request3));
            when(requestMapper.toParticipationRequestDto(any())).thenCallRealMethod();

            EventRequestStatusUpdateResult result = requestService.patchEventRequestsByEventOwner(user1.getId(),
                    event2.getId(), eventRequestStatusUpdateRequest);

            assertEquals(1, result.getConfirmedRequests().size());
            assertEquals(1, result.getRejectedRequests().size());

            ParticipationRequestDto confirmedRequestsDto = result.getConfirmedRequests().get(0);
            ParticipationRequestDto rejectedRequestsDto = result.getRejectedRequests().get(0);

            assertEquals(request4.getId(), confirmedRequestsDto.getId());
            assertEquals(request3.getId(), rejectedRequestsDto.getId());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByIdIn(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(requestRepository, times(2)).saveAll(requestsArgumentCaptor.capture());
            verify(requestRepository, times(1)).findAllByEventIdAndStatus(any(), any());
            verify(requestMapper, times(2)).toParticipationRequestDto(any());

            List<List<Request>> savedRequests = requestsArgumentCaptor.getAllValues();

            assertEquals(2, savedRequests.size());

            List<Request> savedRequestsFirst = savedRequests.get(0);
            List<Request> savedRequestsSecond = savedRequests.get(1);

            assertEquals(1, savedRequestsFirst.size());
            assertEquals(1, savedRequestsSecond.size());

            Request savedRequestFirst1 = savedRequestsFirst.get(0);
            Request savedRequestSecond1 = savedRequestsSecond.get(0);

            assertEquals(request4.getId(), savedRequestFirst1.getId());
            assertEquals(request4.getRequester(), savedRequestFirst1.getRequester());
            assertEquals(request4.getEvent().getId(), savedRequestFirst1.getEvent().getId());
            assertEquals(request4.getCreated(), savedRequestFirst1.getCreated());
            assertEquals(RequestStatus.CONFIRMED, savedRequestFirst1.getStatus());

            assertEquals(request3.getId(), savedRequestSecond1.getId());
            assertEquals(request3.getRequester(), savedRequestSecond1.getRequester());
            assertEquals(request3.getEvent().getId(), savedRequestSecond1.getEvent().getId());
            assertEquals(request3.getCreated(), savedRequestSecond1.getCreated());
            assertEquals(RequestStatus.REJECTED, savedRequestSecond1.getStatus());
        }

        @Test
        public void shouldThrowExceptionIfUserNotEventOwner() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.patchEventRequestsByEventOwner(user2.getId(), event2.getId(),
                            eventRequestStatusUpdateRequest));
            assertEquals("Пользователь не является владельцем.", exception.getMessage());


            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, never()).saveAll(any());
        }

        @Test
        public void shouldThrowExceptionIfRequestsNotFound() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds())).thenReturn(List.of());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> requestService.patchEventRequestsByEventOwner(user1.getId(), event2.getId(),
                            eventRequestStatusUpdateRequest));
            assertEquals("Некоторые запросы на участие не найдены.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByIdIn(any());
            verify(requestRepository, never()).saveAll(any());
        }

        @Test
        public void shouldThrowExceptionIfRequestsStatusNotPending() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds()))
                    .thenReturn(List.of(request2));

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.patchEventRequestsByEventOwner(user1.getId(), event2.getId(),
                            eventRequestStatusUpdateRequest));
            assertEquals("Изменять можно только заявки, находящиеся в ожидании.", exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByIdIn(any());
            verify(requestRepository, never()).saveAll(any());
        }

        @Test
        public void shouldThrowExceptionIfConfirmedRequestLimitReached() {
            confirmedRequests.put(event2.getId(), 2L);
            eventRequestStatusUpdateRequest.setStatus(RequestStatusAction.CONFIRMED);

            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(eventService.getEventById(event2.getId())).thenReturn(event2);
            when(requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds()))
                    .thenReturn(List.of(request4));
            when(statsService.getConfirmedRequests(any())).thenReturn(confirmedRequests);

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> requestService.patchEventRequestsByEventOwner(user1.getId(), event2.getId(),
                            eventRequestStatusUpdateRequest));
            assertEquals(String.format("Достигнут лимит подтвержденных запросов на участие: %s",
                    event2.getParticipantLimit()), exception.getMessage());

            verify(userService, times(1)).getUserById(any());
            verify(eventService, times(1)).getEventById(any());
            verify(requestRepository, times(1)).findAllByIdIn(any());
            verify(statsService, times(1)).getConfirmedRequests(any());
            verify(requestRepository, never()).saveAll(any());
        }
    }
}
