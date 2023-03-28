package ru.practicum.main_service.event.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.event.dto.ParticipationRequestDto;
import ru.practicum.main_service.event.enums.RequestStatus;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Request;
import ru.practicum.main_service.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class RequestMapperTest {
    @InjectMocks
    private RequestMapperImpl requestMapper;

    private final User user = User.builder()
            .id(1L)
            .name("test user")
            .email("test@yandex.ru")
            .build();
    private final Event event = Event.builder()
            .id(1L)
            .build();
    private final Request request = Request.builder()
            .id(1L)
            .event(event)
            .requester(user)
            .created(LocalDateTime.now())
            .status(RequestStatus.PENDING)
            .build();

    @Nested
    class ToParticipationRequestDto {
        @Test
        public void shouldReturnParticipationRequestDto() {
            ParticipationRequestDto result = requestMapper.toParticipationRequestDto(request);

            assertEquals(request.getId(), result.getId());
            assertEquals(request.getEvent().getId(), result.getEvent());
            assertEquals(request.getRequester().getId(), result.getRequester());
            assertEquals(request.getCreated(), result.getCreated());
            assertEquals(request.getStatus(), result.getStatus());
        }

        @Test
        public void shouldReturnNull() {
            ParticipationRequestDto result = requestMapper.toParticipationRequestDto(null);

            assertNull(result);
        }
    }
}
