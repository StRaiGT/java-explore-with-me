package ru.practicum.main_service.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.compilation.controller.CompilationPublicController;
import ru.practicum.main_service.compilation.dto.CompilationDto;
import ru.practicum.main_service.compilation.service.CompilationService;
import ru.practicum.main_service.event.dto.EventShortDto;

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

@WebMvcTest(controllers = CompilationPublicController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationPublicControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private CompilationService compilationService;

    private final EventShortDto eventShortDto = EventShortDto.builder()
            .id(2L)
            .build();
    private final CompilationDto compilationDto = CompilationDto.builder()
            .id(1L)
            .title("test title")
            .pinned(false)
            .events(List.of(eventShortDto))
            .build();

    @Nested
    class GetAll {
        @Test
        public void shouldGet() throws Exception {
            when(compilationService.getAll(any(), any())).thenReturn(List.of(compilationDto));

            mvc.perform(get("/compilations?pinned=false&from=0&size=10")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(compilationDto))));

            verify(compilationService, times(1)).getAll(any(), any());
        }

        @Test
        public void shouldGetByDefaultAndPinnedIsNull() throws Exception {
            when(compilationService.getAll(any(), any())).thenReturn(List.of(compilationDto));

            mvc.perform(get("/compilations")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(compilationDto))));

            verify(compilationService, times(1)).getAll(any(), any());
        }

        @Test
        public void shouldGetEmpty() throws Exception {
            when(compilationService.getAll(any(), any())).thenReturn(List.of());

            mvc.perform(get("/compilations")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(compilationService, times(1)).getAll(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/compilations?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).getAll(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/compilations?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).getAll(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/compilations?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).getAll(any(), any());
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGet() throws Exception {
            when(compilationService.getById(any())).thenReturn(compilationDto);

            mvc.perform(get("/compilations/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).getById(any());
        }
    }
}
