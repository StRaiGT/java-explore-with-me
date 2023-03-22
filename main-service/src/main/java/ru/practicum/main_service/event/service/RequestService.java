package ru.practicum.main_service.event.service;

import ru.practicum.main_service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main_service.event.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getEventRequestsByRequester(Long userId);

    ParticipationRequestDto createEventRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelEventRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult patchEventRequestsByEventOwner(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
