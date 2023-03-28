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
import ru.practicum.main_service.event.dto.ParticipationRequestDto;
import ru.practicum.main_service.event.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestPrivateController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestPrivateControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private RequestService requestService;

    private final ParticipationRequestDto participationRequestDto1 = ParticipationRequestDto.builder()
            .id(1L)
            .build();
    private final ParticipationRequestDto participationRequestDto2 = ParticipationRequestDto.builder()
            .id(2L)
            .build();

    @Nested
    class GetEventRequestsByRequester {
        @Test
        public void shouldGet() throws Exception {
            when(requestService.getEventRequestsByRequester(any()))
                    .thenReturn(List.of(participationRequestDto1, participationRequestDto2));

            mvc.perform(get("/users/1/requests")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(
                            List.of(participationRequestDto1, participationRequestDto2))));

            verify(requestService, times(1)).getEventRequestsByRequester(any());
        }
    }

    @Nested
    class CreateEventRequest {
        @Test
        public void shouldCreate() throws Exception {
            when(requestService.createEventRequest(any(), any())).thenReturn(participationRequestDto1);

            mvc.perform(post("/users/1/requests?eventId=1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(participationRequestDto1)));

            verify(requestService, times(1)).createEventRequest(any(), any());
        }
    }

    @Nested
    class CancelEventRequest {
        @Test
        public void shouldPatch() throws Exception {
            when(requestService.cancelEventRequest(any(), any())).thenReturn(participationRequestDto1);

            mvc.perform(patch("/users/1/requests/1/cancel")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(participationRequestDto1)));

            verify(requestService, times(1)).cancelEventRequest(any(), any());
        }
    }
}
