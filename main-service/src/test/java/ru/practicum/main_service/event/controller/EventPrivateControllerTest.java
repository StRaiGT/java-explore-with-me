package ru.practicum.main_service.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main_service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.dto.ParticipationRequestDto;
import ru.practicum.main_service.event.dto.UpdateEventUserRequest;
import ru.practicum.main_service.event.enums.EventStateAction;
import ru.practicum.main_service.event.enums.RequestStatusAction;
import ru.practicum.main_service.event.service.EventService;
import ru.practicum.main_service.event.service.RequestService;
import ru.practicum.stats_common.StatsCommonUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventPrivateController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventPrivateControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private RequestService requestService;

    private final EventShortDto eventShortDto1 = EventShortDto.builder()
            .id(1L)
            .build();
    private final EventShortDto eventShortDto2 = EventShortDto.builder()
            .id(2L)
            .build();
    private final EventFullDto eventFullDto1 = EventFullDto.builder()
            .id(eventShortDto1.getId())
            .build();
    private final LocationDto location = LocationDto.builder()
            .lat(15.0175F)
            .lon(-14.4520F)
            .build();
    private final ParticipationRequestDto participationRequestDto1 = ParticipationRequestDto.builder()
            .id(1L)
            .build();
    private final ParticipationRequestDto participationRequestDto2 = ParticipationRequestDto.builder()
            .id(2L)
            .build();
    private final EventRequestStatusUpdateResult eventRequestStatusUpdateResult = EventRequestStatusUpdateResult.builder()
            .confirmedRequests(List.of(participationRequestDto1, participationRequestDto2))
            .rejectedRequests(List.of())
            .build();
    private NewEventDto newEventDto1;
    private UpdateEventUserRequest updateEventUserRequest;
    private EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest;

    @Nested
    class GetAllEventsByPrivate {
        @Test
        public void shouldGet() throws Exception {
            when(eventService.getAllEventsByPrivate(any(), any())).thenReturn(List.of(eventShortDto1, eventShortDto2));

            mvc.perform(get("/users/1/events?from=0&size=1000")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventShortDto1, eventShortDto2))));

            verify(eventService, times(1)).getAllEventsByPrivate(any(), any());
        }

        @Test
        public void shouldGetByDefault() throws Exception {
            when(eventService.getAllEventsByPrivate(any(), any())).thenReturn(List.of(eventShortDto1, eventShortDto2));

            mvc.perform(get("/users/1/events")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventShortDto1, eventShortDto2))));

            verify(eventService, times(1)).getAllEventsByPrivate(any(), any());
        }

        @Test
        public void shouldGetByEmpty() throws Exception {
            when(eventService.getAllEventsByPrivate(any(), any())).thenReturn(List.of());

            mvc.perform(get("/users/1/events")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(eventService, times(1)).getAllEventsByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/users/1/events?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).getAllEventsByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/users/1/events?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).getAllEventsByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/users/1/events?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).getAllEventsByPrivate(any(), any());
        }
    }

    @Nested
    class CreateEventByPrivate {
        @BeforeEach
        public void beforeEach() {
            newEventDto1 = NewEventDto.builder()
                    .annotation("test annotation event")
                    .title("test title")
                    .category(1L)
                    .description("test description event")
                    .eventDate(LocalDateTime.parse("2022-01-06 13:30:00", StatsCommonUtils.DT_FORMATTER))
                    .location(location)
                    .paid(true)
                    .participantLimit(0)
                    .requestModeration(false)
                    .build();
        }

        @Test
        public void shouldCreate() throws Exception {
            when(eventService.createEventByPrivate(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationIsNull() throws Exception {
            newEventDto1.setAnnotation(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationIsEmpty() throws Exception {
            newEventDto1.setAnnotation("");

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationIsBlank() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_ANNOTATION) {
                sb.append(" ");
            }
            newEventDto1.setAnnotation(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_ANNOTATION - 1) {
                sb.append("a");
            }
            newEventDto1.setAnnotation(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_ANNOTATION) {
                sb.append("a");
            }
            newEventDto1.setAnnotation(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionIsNull() throws Exception {
            newEventDto1.setDescription(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionIsEmpty() throws Exception {
            newEventDto1.setDescription("");

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionIsBlank() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_DESCRIPTION) {
                sb.append(" ");
            }
            newEventDto1.setDescription(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_DESCRIPTION - 1) {
                sb.append("a");
            }
            newEventDto1.setDescription(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_DESCRIPTION) {
                sb.append("a");
            }
            newEventDto1.setDescription(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsNull() throws Exception {
            newEventDto1.setTitle(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsEmpty() throws Exception {
            newEventDto1.setTitle("");

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsBlank() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE) {
                sb.append(" ");
            }
            newEventDto1.setTitle(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE - 1) {
                sb.append("a");
            }
            newEventDto1.setTitle(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_TITLE) {
                sb.append("a");
            }
            newEventDto1.setTitle(sb.toString());

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfCategoryIsNull() throws Exception {
            newEventDto1.setCategory(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfEventDateIsNull() throws Exception {
            newEventDto1.setEventDate(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldCreateIfParticipantLimitIsNull() throws Exception {
            newEventDto1.setParticipantLimit(null);

            when(eventService.createEventByPrivate(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfParticipantLimitIsNegative() throws Exception {
            newEventDto1.setParticipantLimit(-1);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationIsNull() throws Exception {
            newEventDto1.setLocation(null);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLonIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(14.1254F)
                    .lon(null)
                    .build();
            newEventDto1.setLocation(newLocationDto);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLatIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(null)
                    .lon(14.1254F)
                    .build();
            newEventDto1.setLocation(newLocationDto);

            mvc.perform(post("/users/1/events")
                            .content(mapper.writeValueAsString(newEventDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).createEventByPrivate(any(), any());
        }
    }

    @Nested
    class GetEventByPrivate {
        @Test
        public void shouldGet() throws Exception {
            when(eventService.getEventByPrivate(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(get("/users/1/events/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).getEventByPrivate(any(), any());
        }
    }

    @Nested
    class PatchEventByPrivate {
        @BeforeEach
        public void beforeEach() {
            updateEventUserRequest = UpdateEventUserRequest.builder()
                    .annotation("test annotation event")
                    .title("test title")
                    .category(1L)
                    .description("test description event")
                    .eventDate(LocalDateTime.parse("2022-01-06 13:30:00", StatsCommonUtils.DT_FORMATTER))
                    .location(location)
                    .paid(true)
                    .participantLimit(0)
                    .requestModeration(false)
                    .stateAction(EventStateAction.SEND_TO_REVIEW)
                    .build();
        }

        @Test
        public void shouldGet() throws Exception {
            when(eventService.patchEventByPrivate(any(), any(), any())).thenReturn(eventFullDto1);

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldGetIfUpdateEventUserRequestIsEmpty() throws Exception {
            updateEventUserRequest = new UpdateEventUserRequest();

            when(eventService.patchEventByPrivate(any(), any(), any())).thenReturn(eventFullDto1);

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_ANNOTATION - 1) {
                sb.append("a");
            }
            updateEventUserRequest.setAnnotation(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_ANNOTATION) {
                sb.append("a");
            }
            updateEventUserRequest.setAnnotation(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_DESCRIPTION - 1) {
                sb.append("a");
            }
            updateEventUserRequest.setDescription(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_DESCRIPTION) {
                sb.append("a");
            }
            updateEventUserRequest.setDescription(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE - 1) {
                sb.append("a");
            }
            updateEventUserRequest.setTitle(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_TITLE) {
                sb.append("a");
            }
            updateEventUserRequest.setTitle(sb.toString());

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfParticipantLimitIsNegative() throws Exception {
            updateEventUserRequest.setParticipantLimit(-1);

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLatIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(null)
                    .lon(14.1245F)
                    .build();
            updateEventUserRequest.setLocation(newLocationDto);

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLonIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(14.1245F)
                    .lon(null)
                    .build();
            updateEventUserRequest.setLocation(newLocationDto);

            mvc.perform(patch("/users/1/events/1")
                            .content(mapper.writeValueAsString(updateEventUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByPrivate(any(), any(), any());
        }
    }

    @Nested
    class GetEventRequestsByEventOwner {
        @Test
        public void shouldGet() throws Exception {
            when(requestService.getEventRequestsByEventOwner(any(), any()))
                    .thenReturn(List.of(participationRequestDto1, participationRequestDto2));

            mvc.perform(get("/users/1/events/1/requests")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(
                            List.of(participationRequestDto1, participationRequestDto2))));

            verify(requestService, times(1)).getEventRequestsByEventOwner(any(), any());
        }

        @Test
        public void shouldGetEmpty() throws Exception {
            when(requestService.getEventRequestsByEventOwner(any(), any())).thenReturn(List.of());

            mvc.perform(get("/users/1/events/1/requests")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(requestService, times(1)).getEventRequestsByEventOwner(any(), any());
        }
    }

    @Nested
    class PatchEventRequestsByEventOwner {
        @BeforeEach
        public void beforeEach() {
            eventRequestStatusUpdateRequest = EventRequestStatusUpdateRequest.builder()
                    .requestIds(List.of(1L, 2L))
                    .status(RequestStatusAction.CONFIRMED)
                    .build();
        }

        @Test
        public void shouldPatch() throws Exception {
            when(requestService.patchEventRequestsByEventOwner(any(), any(), any())).thenReturn(eventRequestStatusUpdateResult);

            mvc.perform(patch("/users/1/events/1/requests")
                            .content(mapper.writeValueAsString(eventRequestStatusUpdateRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventRequestStatusUpdateResult)));

            verify(requestService, times(1)).patchEventRequestsByEventOwner(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRequestIdsIsNull() throws Exception {
            eventRequestStatusUpdateRequest.setRequestIds(null);

            mvc.perform(patch("/users/1/events/1/requests")
                            .content(mapper.writeValueAsString(eventRequestStatusUpdateRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(requestService, never()).patchEventRequestsByEventOwner(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRequestIdsIsEmpty() throws Exception {
            eventRequestStatusUpdateRequest.setRequestIds(List.of());

            mvc.perform(patch("/users/1/events/1/requests")
                            .content(mapper.writeValueAsString(eventRequestStatusUpdateRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(requestService, never()).patchEventRequestsByEventOwner(any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfStatusActionIsNull() throws Exception {
            eventRequestStatusUpdateRequest.setStatus(null);

            mvc.perform(patch("/users/1/events/1/requests")
                            .content(mapper.writeValueAsString(eventRequestStatusUpdateRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(requestService, never()).patchEventRequestsByEventOwner(any(), any(), any());
        }
    }
}
