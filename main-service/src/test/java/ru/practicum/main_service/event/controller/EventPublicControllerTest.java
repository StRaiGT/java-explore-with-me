package ru.practicum.main_service.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.service.EventService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventPublicController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventPublicControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private EventService eventService;

    private final EventShortDto eventShortDto1 = EventShortDto.builder()
            .id(1L)
            .build();
    private final EventShortDto eventShortDto2 = EventShortDto.builder()
            .id(2L)
            .build();
    private final EventFullDto eventFullDto1 = EventFullDto.builder()
            .id(eventShortDto1.getId())
            .build();

    @Nested
    class GetEventsByPublic {
        @Test
        public void shouldGet() throws Exception {
            when(eventService.getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(eventShortDto1, eventShortDto2));

            mvc.perform(get("/events?text=0&" +
                            "categories=0&" +
                            "paid=true&" +
                            "rangeStart=2022-01-06 13:30:00&" +
                            "rangeEnd=2097-09-06 13:30:00&" +
                            "onlyAvailable=false&" +
                            "sort=EVENT_DATE&" +
                            "from=0&" +
                            "size=1000")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventShortDto1, eventShortDto2))));

            verify(eventService, times(1))
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldGetWithEmptyParameters() throws Exception {
            when(eventService.getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(eventShortDto1, eventShortDto2));

            mvc.perform(get("/events")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(eventShortDto1, eventShortDto2))));

            verify(eventService, times(1))
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRangeStartNoPattern() throws Exception {
            mvc.perform(get("/events?rangeStart=2022-01-06T13:30:00")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfRangeEndNoPattern() throws Exception {
            mvc.perform(get("/events?rangeEnd=2097-09-06T13:30:00")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/events?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/events?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/events?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(eventService, never())
                    .getEventsByPublic(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class GetEventByPublic {
        @Test
        public void shouldGet() throws Exception {
            when(eventService.getEventByPublic(any(), any())).thenReturn(eventFullDto1);

            mvc.perform(get("/events/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(eventFullDto1)));

            verify(eventService, times(1)).getEventByPublic(any(), any());
        }
    }
}
