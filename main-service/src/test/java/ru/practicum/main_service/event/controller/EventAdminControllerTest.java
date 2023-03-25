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
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.dto.UpdateEventAdminRequest;
import ru.practicum.main_service.event.enums.EventStateAction;
import ru.practicum.main_service.event.service.EventService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventAdminController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventAdminControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private EventService eventService;

    private final EventFullDto eventFullDto1 = EventFullDto.builder()
            .id(1L)
            .build();
    private final EventFullDto eventFullDto2 = EventFullDto.builder()
            .id(2L)
            .build();
    private final LocationDto location = LocationDto.builder()
            .lat(15.0175F)
            .lon(-14.4520F)
            .build();
    private UpdateEventAdminRequest updateEventAdminRequest;

    @Nested
    class GetEventsByAdmin {
        @Test
        public void shouldGet() throws Exception {
            when(eventService.getEventsByAdmin(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(eventFullDto1, eventFullDto2));

            mvc.perform(get("/admin/events?users=0&" +
                            "states=PUBLISHED&" +
                            "categories=0&" +
                            "rangeStart=2022-01-06 13:30:00&" +
                            "rangeEnd=2097-09-06 13:30:00&" +
                            "from=0&" +
                            "size=1000")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventFullDto1, eventFullDto2))));

            verify(eventService, times(1))
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldGetWithoutParameters() throws Exception {
            when(eventService.getEventsByAdmin(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(eventFullDto1, eventFullDto2));

            mvc.perform(get("/admin/events")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventFullDto1, eventFullDto2))));

            verify(eventService, times(1))
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldGetEmpty() throws Exception {
            when(eventService.getEventsByAdmin(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            mvc.perform(get("/admin/events")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(eventService, times(1))
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRangeStartNoPattern() throws Exception {
            mvc.perform(get("/admin/events?rangeStart=2022-01-06T13:30:00")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRangeEndNoPattern() throws Exception {
            mvc.perform(get("/admin/events?rangeEnd=2097-09-06T13:30:00")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/admin/events?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/admin/events?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/admin/events?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class PatchEventByAdmin {
        @BeforeEach
        public void beforeEach() {
            updateEventAdminRequest = UpdateEventAdminRequest.builder()
                    .annotation("test annotation event")
                    .title("test title")
                    .category(1L)
                    .description("test description event")
                    .eventDate(LocalDateTime.parse("2022-01-06 13:30:00", StatsCommonUtils.DT_FORMATTER))
                    .location(location)
                    .paid(true)
                    .participantLimit(0)
                    .requestModeration(false)
                    .stateAction(EventStateAction.PUBLISH_EVENT)
                    .build();
        }

        @Test
        public void shouldPatch() throws Exception {
            when(eventService.patchEventByAdmin(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldPatchIfUpdateEventAdminRequestIsEmpty() throws Exception {
            updateEventAdminRequest = new UpdateEventAdminRequest();

            when(eventService.patchEventByAdmin(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_ANNOTATION - 1) {
                sb.append("a");
            }
            updateEventAdminRequest.setAnnotation(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfAnnotationGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_ANNOTATION) {
                sb.append("a");
            }
            updateEventAdminRequest.setAnnotation(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_DESCRIPTION - 1) {
                sb.append("a");
            }
            updateEventAdminRequest.setDescription(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfDescriptionGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_DESCRIPTION) {
                sb.append("a");
            }
            updateEventAdminRequest.setDescription(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE - 1) {
                sb.append("a");
            }
            updateEventAdminRequest.setTitle(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_TITLE) {
                sb.append("a");
            }
            updateEventAdminRequest.setTitle(sb.toString());

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLatIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(null)
                    .lon(10.154F)
                    .build();
            updateEventAdminRequest.setLocation(newLocationDto);

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfLocationLonIsNull() throws Exception {
            LocationDto newLocationDto = LocationDto.builder()
                    .lat(10.154F)
                    .lon(null)
                    .build();
            updateEventAdminRequest.setLocation(newLocationDto);

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfParticipantLimitIsNegative() throws Exception {
            updateEventAdminRequest.setParticipantLimit(-1);

            mvc.perform(patch("/admin/events/1")
                            .content(mapper.writeValueAsString(updateEventAdminRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never()).patchEventByAdmin(any(), any());
        }
    }
}
