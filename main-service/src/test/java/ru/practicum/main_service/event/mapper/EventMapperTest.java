package ru.practicum.main_service.event.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.category.dto.CategoryDto;
import ru.practicum.main_service.category.mapper.CategoryMapperImpl;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.user.dto.UserShortDto;
import ru.practicum.main_service.user.mapper.UserMapperImpl;
import ru.practicum.main_service.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventMapperTest {
    @Mock
    private UserMapperImpl userMapper;

    @Mock
    private CategoryMapperImpl categoryMapper;

    @Mock
    private LocationMapperImpl locationMapper;

    @InjectMocks
    private EventMapperImpl eventMapper;

    private final User user = User.builder()
            .id(1L)
            .name("test name")
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
            .id(category.getId())
            .name(category.getName())
            .build();
    private final LocationDto locationDto = LocationDto.builder()
            .lat(11.1524F)
            .lon(-5.0010F)
            .build();
    private final Location location = Location.builder()
            .id(1L)
            .lat(locationDto.getLat())
            .lon(locationDto.getLon())
            .build();
    private final NewEventDto newEventDto = NewEventDto.builder()
            .title("test title")
            .annotation("test annotation")
            .description("test description")
            .eventDate(LocalDateTime.now().plusDays(2))
            .category(category.getId())
            .location(locationDto)
            .paid(false)
            .participantLimit(0)
            .requestModeration(true)
            .build();
    private final Event event = Event.builder()
            .id(1L)
            .title(newEventDto.getTitle())
            .annotation(newEventDto.getAnnotation())
            .description(newEventDto.getDescription())
            .eventDate(newEventDto.getEventDate())
            .category(category)
            .location(location)
            .paid(newEventDto.getPaid())
            .participantLimit(newEventDto.getParticipantLimit())
            .requestModeration(newEventDto.getRequestModeration())
            .initiator(user)
            .state(EventState.PENDING)
            .createdOn(LocalDateTime.now())
            .publishedOn(null)
            .build();
    private final EventFullDto eventFullDto = EventFullDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .annotation(event.getAnnotation())
            .description(event.getDescription())
            .eventDate(event.getEventDate())
            .category(categoryDto)
            .location(locationDto)
            .paid(event.getPaid())
            .participantLimit(event.getParticipantLimit())
            .requestModeration(event.getRequestModeration())
            .initiator(userShortDto)
            .state(event.getState())
            .createdOn(event.getCreatedOn())
            .publishedOn(event.getPublishedOn())
            .confirmedRequests(10L)
            .views(100L)
            .build();
    private final EventShortDto eventShortDto = EventShortDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .annotation(event.getAnnotation())
            .eventDate(event.getEventDate())
            .category(categoryDto)
            .paid(event.getPaid())
            .initiator(userShortDto)
            .confirmedRequests(5L)
            .views(20L)
            .build();

    @Nested
    class ToEvent {
        @Test
        public void shouldReturnLocation() {
            Event result = eventMapper.toEvent(newEventDto, user, category, location, event.getCreatedOn(), event.getState());

            assertNull(result.getId());
            assertEquals(event.getTitle(), result.getTitle());
            assertEquals(event.getAnnotation(), result.getAnnotation());
            assertEquals(event.getDescription(), result.getDescription());
            assertEquals(event.getEventDate(), result.getEventDate());
            assertEquals(event.getCategory(), result.getCategory());
            assertEquals(event.getLocation(), result.getLocation());
            assertEquals(event.getPaid(), result.getPaid());
            assertEquals(event.getParticipantLimit(), result.getParticipantLimit());
            assertEquals(event.getRequestModeration(), result.getRequestModeration());
            assertEquals(event.getInitiator(), result.getInitiator());
            assertEquals(event.getState(), result.getState());
            assertEquals(event.getCreatedOn(), result.getCreatedOn());
            assertNull(result.getPublishedOn());
        }

        @Test
        public void shouldReturnNull() {
            Event result = eventMapper.toEvent(null, null, null, null, null, null);

            assertNull(result);
        }
    }

    @Nested
    class ToEventFullDto {
        @Test
        public void shouldReturnEventFullDto() {
            when(userMapper.toUserShortDto(any())).thenCallRealMethod();
            when(categoryMapper.toCategoryDto(any())).thenCallRealMethod();
            when(locationMapper.toLocationDto(any())).thenCallRealMethod();

            EventFullDto result = eventMapper.toEventFullDto(event, eventFullDto.getConfirmedRequests(), eventFullDto.getViews());

            assertEquals(eventFullDto.getId(), result.getId());
            assertEquals(eventFullDto.getTitle(), result.getTitle());
            assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
            assertEquals(eventFullDto.getDescription(), result.getDescription());
            assertEquals(eventFullDto.getEventDate(), result.getEventDate());
            assertEquals(eventFullDto.getCategory(), result.getCategory());
            assertEquals(eventFullDto.getLocation(), result.getLocation());
            assertEquals(eventFullDto.getPaid(), result.getPaid());
            assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
            assertEquals(eventFullDto.getRequestModeration(), result.getRequestModeration());
            assertEquals(eventFullDto.getInitiator(), result.getInitiator());
            assertEquals(eventFullDto.getState(), result.getState());
            assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
            assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
            assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
            assertEquals(eventFullDto.getViews(), result.getViews());

            verify(userMapper, times(1)).toUserShortDto(any());
            verify(categoryMapper, times(1)).toCategoryDto(any());
            verify(locationMapper, times(1)).toLocationDto(any());
        }

        @Test
        public void shouldReturnNull() {
            EventFullDto result = eventMapper.toEventFullDto(null, null, null);

            assertNull(result);
        }
    }

    @Nested
    class ToEventShortDto {
        @Test
        public void shouldReturnEventShortDto() {
            when(userMapper.toUserShortDto(any())).thenCallRealMethod();
            when(categoryMapper.toCategoryDto(any())).thenCallRealMethod();

            EventShortDto result = eventMapper.toEventShortDto(event, eventShortDto.getConfirmedRequests(), eventShortDto.getViews());

            assertEquals(eventShortDto.getId(), result.getId());
            assertEquals(eventShortDto.getTitle(), result.getTitle());
            assertEquals(eventShortDto.getAnnotation(), result.getAnnotation());
            assertEquals(eventShortDto.getEventDate(), result.getEventDate());
            assertEquals(eventShortDto.getCategory(), result.getCategory());
            assertEquals(eventShortDto.getPaid(), result.getPaid());
            assertEquals(eventShortDto.getInitiator(), result.getInitiator());
            assertEquals(eventShortDto.getConfirmedRequests(), result.getConfirmedRequests());
            assertEquals(eventShortDto.getViews(), result.getViews());

            verify(userMapper, times(1)).toUserShortDto(any());
            verify(categoryMapper, times(1)).toCategoryDto(any());
        }

        @Test
        public void shouldReturnNull() {
            EventShortDto result = eventMapper.toEventShortDto(null, null, null);

            assertNull(result);
        }
    }
}
