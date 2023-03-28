package ru.practicum.main_service.compilation;

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
import ru.practicum.main_service.compilation.controller.CompilationAdminController;
import ru.practicum.main_service.compilation.dto.CompilationDto;
import ru.practicum.main_service.compilation.dto.NewCompilationDto;
import ru.practicum.main_service.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main_service.compilation.service.CompilationService;
import ru.practicum.main_service.event.dto.EventShortDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationAdminController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationAdminControllerTest {
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

    private NewCompilationDto newCompilationDto;
    private UpdateCompilationRequest updateCompilationRequest;

    @Nested
    class Create {
        @BeforeEach
        public void beforeEach() {
            newCompilationDto = NewCompilationDto.builder()
                    .title("test title")
                    .pinned(false)
                    .events(List.of(1L))
                    .build();
        }

        @Test
        public void shouldCreate() throws Exception {
            when(compilationService.create(any())).thenReturn(compilationDto);

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).create(any());
        }

        @Test
        public void shouldCreateIfPinnedIsNull() throws Exception {
            newCompilationDto.setPinned(null);

            when(compilationService.create(any())).thenReturn(compilationDto);

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).create(any());
        }

        @Test
        public void shouldCreateIfEventsIsNull() throws Exception {
            newCompilationDto.setEvents(null);

            when(compilationService.create(any())).thenReturn(compilationDto);

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).create(any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsNull() throws Exception {
            newCompilationDto.setTitle(null);

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).create(any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsEmpty() throws Exception {
            newCompilationDto.setTitle("");

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).create(any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsBlank() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE) {
                sb.append(" ");
            }
            newCompilationDto.setTitle(sb.toString());

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).create(any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE - 1) {
                sb.append("a");
            }
            newCompilationDto.setTitle(sb.toString());

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).create(any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_TITLE) {
                sb.append("a");
            }
            newCompilationDto.setTitle(sb.toString());

            mvc.perform(post("/admin/compilations")
                            .content(mapper.writeValueAsString(newCompilationDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).create(any());
        }
    }

    @Nested
    class Patch {
        @BeforeEach
        public void beforeEach() {
            updateCompilationRequest = UpdateCompilationRequest.builder()
                    .title("test title")
                    .pinned(false)
                    .events(List.of(1L))
                    .build();
        }

        @Test
        public void shouldPatch() throws Exception {
            when(compilationService.patch(any(), any())).thenReturn(compilationDto);

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).patch(any(), any());
        }

        @Test
        public void shouldPatchIfPinnedIsNull() throws Exception {
            updateCompilationRequest.setPinned(null);

            when(compilationService.patch(any(), any())).thenReturn(compilationDto);

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).patch(any(), any());
        }

        @Test
        public void shouldPatchIfEventsIsNull() throws Exception {
            updateCompilationRequest.setEvents(null);

            when(compilationService.patch(any(), any())).thenReturn(compilationDto);

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).patch(any(), any());
        }

        @Test
        public void shouldPatchIfTitleIsNull() throws Exception {
            updateCompilationRequest.setTitle(null);

            when(compilationService.patch(any(), any())).thenReturn(compilationDto);

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(compilationDto)));

            verify(compilationService, times(1)).patch(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsEmpty() throws Exception {
            updateCompilationRequest.setTitle("");

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).patch(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsLessMin() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < MainCommonUtils.MIN_LENGTH_TITLE - 1) {
                sb.append("a");
            }
            updateCompilationRequest.setTitle(sb.toString());

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).patch(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfTitleIsGreaterMax() throws Exception {
            StringBuilder sb = new StringBuilder();
            while (sb.length() <= MainCommonUtils.MAX_LENGTH_TITLE) {
                sb.append("a");
            }
            updateCompilationRequest.setTitle(sb.toString());

            mvc.perform(patch("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(compilationService, never()).patch(any(), any());
        }
    }

    @Nested
    class Delete {
        @Test
        public void shouldDelete() throws Exception {
            mvc.perform(delete("/admin/compilations/1")
                            .content(mapper.writeValueAsString(updateCompilationRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(compilationService, times(1)).deleteById(any());
        }
    }
}
