package ru.practicum.main_service.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main_service.event.dto.ParticipationRequestDto;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.enums.RequestStatus;
import ru.practicum.main_service.event.enums.RequestStatusAction;
import ru.practicum.main_service.event.mapper.RequestMapper;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Request;
import ru.practicum.main_service.event.repository.RequestRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final EventService eventService;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getEventRequestsByRequester(Long userId) {
        log.info("Вывод списка запросов на участие в чужих событиях пользователем с id {}", userId);

        userService.getUserById(userId);

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createEventRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие в событии с id {} пользователем с id {}", eventId, userId);

        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Нельзя создавать запрос на собственное событие.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("Нельзя создавать запрос на неопубликованное событие.");
        }

        Optional<Request> oldRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);

        if (oldRequest.isPresent()) {
            throw new ForbiddenException("Создавать повторный запрос запрещено.");
        }

        if ((event.getParticipantLimit() != 0) &&
                (requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size() >=
                        event.getParticipantLimit())) {
            throw new ForbiddenException("Достигнут лимит подтвержденных запросов на участие.");
        }

        Request newRequest = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toParticipationRequestDto(requestRepository.save(newRequest));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelEventRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с id {} на участие в событии пользователем с id {}", requestId, userId);

        userService.getUserById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявки на участие с таким id не существует."));

        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new ForbiddenException("Пользователь не является владельцем запроса на участие.");
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId) {
        log.info("Вывод списка запросов на участие в событии с id {} владельцем с id {}", eventId, userId);

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Пользователь не является владельцем события.");
        }

        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult patchEventRequestsByEventOwner(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Обновление запросов на участие в событии с id {} владельцем с id {} и параметрами {}",
                eventId, userId, eventRequestStatusUpdateRequest);

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Пользователь не является владельцем события.");
        }

        if (!event.getRequestModeration() ||
                event.getParticipantLimit() == 0 ||
                eventRequestStatusUpdateRequest.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        List<Request> confirmedList = new ArrayList<>();
        List<Request> rejectedList = new ArrayList<>();

        List<Request> requests = requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (requests.size() != eventRequestStatusUpdateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые запросы на участие не найдены.");
        }

        if (!requests.stream()
                .map(Request::getStatus)
                .allMatch(RequestStatus.PENDING::equals)) {
            throw new ForbiddenException("Изменять можно только заявки, находящиеся в ожидании.");
        }

        if (eventRequestStatusUpdateRequest.getStatus().equals(RequestStatusAction.REJECTED)) {
            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            requestRepository.saveAll(requests);
            rejectedList.addAll(requests);
        } else {
            Map<Long, Long> requestStats = new HashMap<>();

            requestRepository.getConfirmedRequests(List.of(eventId))
                    .forEach(stat -> requestStats.put(stat.getEventId(), stat.getConfirmedRequests()));

            Long confirmedRequests = requestStats.getOrDefault(eventId, 0L);

            if ((confirmedRequests + eventRequestStatusUpdateRequest.getRequestIds().size()) > event.getParticipantLimit()) {
                throw new ForbiddenException("Достигнут лимит подтвержденных запросов на участие.");
            }

            requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            requestRepository.saveAll(requests);
            confirmedList.addAll(requests);

            if ((confirmedRequests + eventRequestStatusUpdateRequest.getRequestIds().size()) >= event.getParticipantLimit()) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
                if (!pendingRequests.isEmpty()) {
                    pendingRequests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
                    requestRepository.saveAll(pendingRequests);
                }
            }
        }

        List<ParticipationRequestDto> confirmedListDto = confirmedList.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedListDto = rejectedList.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(confirmedListDto, rejectedListDto);
    }
}
