package ru.practicum.main_service.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.comment.controller.CommentPublicController;
import ru.practicum.main_service.comment.dto.CommentDto;
import ru.practicum.main_service.comment.service.CommentService;
import ru.practicum.main_service.user.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentPublicController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentPublicControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private CommentService commentService;

    private final UserShortDto userShortDto = UserShortDto.builder()
            .id(1L)
            .name("test name")
            .build();
    private final CommentDto commentDto1 = CommentDto.builder()
            .id(1L)
            .text("test text 1")
            .author(userShortDto)
            .eventId(1L)
            .createdOn(LocalDateTime.now().minusHours(2))
            .editedOn(LocalDateTime.now().minusHours(1))
            .build();
    private final CommentDto commentDto2 = CommentDto.builder()
            .id(2L)
            .text("test text 2")
            .author(userShortDto)
            .eventId(1L)
            .createdOn(LocalDateTime.now().minusHours(2))
            .editedOn(null)
            .build();

    @Nested
    class GetCommentsByPublic {
        @Test
        public void shouldGet() throws Exception {
            when(commentService.getCommentsByPublic(any(), any())).thenReturn(List.of(commentDto1, commentDto2));

            mvc.perform(get("/comments?eventId=1&from=0&size=10")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(commentDto1, commentDto2))));

            verify(commentService, times(1)).getCommentsByPublic(any(), any());
        }

        @Test
        public void shouldGetDefault() throws Exception {
            when(commentService.getCommentsByPublic(any(), any())).thenReturn(List.of(commentDto1, commentDto2));

            mvc.perform(get("/comments?eventId=1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(commentDto1, commentDto2))));

            verify(commentService, times(1)).getCommentsByPublic(any(), any());
        }

        @Test
        public void shouldGetEmpty() throws Exception {
            when(commentService.getCommentsByPublic(any(), any())).thenReturn(List.of());

            mvc.perform(get("/comments?eventId=1&from=0&size=10")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(commentService, times(1)).getCommentsByPublic(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/comments?eventId=1&from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).getCommentsByPublic(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/comments?eventId=1&size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).getCommentsByPublic(any(), any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/comments?eventId=1&size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).getCommentsByPublic(any(), any());
        }
    }

    @Nested
    class GetCommentByPublic {
        @Test
        public void shouldGet() throws Exception {
            when(commentService.getCommentByPublic(any())).thenReturn(commentDto1);

            mvc.perform(get("/comments/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(commentDto1)));

            verify(commentService, times(1)).getCommentByPublic(any());
        }
    }
}
