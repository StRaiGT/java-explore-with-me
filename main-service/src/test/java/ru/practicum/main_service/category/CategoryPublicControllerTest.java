package ru.practicum.main_service.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.category.controller.CategoryPublicController;
import ru.practicum.main_service.category.dto.CategoryDto;
import ru.practicum.main_service.category.service.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryPublicController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryPublicControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    private final CategoryDto categoryDto1 = CategoryDto.builder()
            .id(1L)
            .name("test category 1")
            .build();
    private final CategoryDto categoryDto2 = CategoryDto.builder()
            .id(2L)
            .name("test category 2")
            .build();

    @Nested
    class GetAll {
        @Test
        public void shouldGet() throws Exception {
            when(categoryService.getAll(ArgumentMatchers.any())).thenReturn(List.of(categoryDto1, categoryDto2));

            mvc.perform(get("/categories?from=0&size=10")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(categoryDto1, categoryDto2))));

            verify(categoryService, times(1)).getAll(ArgumentMatchers.any());
        }

        @Test
        public void shouldGetByDefault() throws Exception {
            when(categoryService.getAll(ArgumentMatchers.any())).thenReturn(List.of(categoryDto1, categoryDto2));

            mvc.perform(get("/categories")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(categoryDto1, categoryDto2))));

            verify(categoryService, times(1)).getAll(ArgumentMatchers.any());
        }

        @Test
        public void shouldGetEmptyByDefault() throws Exception {
            when(categoryService.getAll(ArgumentMatchers.any())).thenReturn(List.of());

            mvc.perform(get("/categories")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(categoryService, times(1)).getAll(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/categories?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).getAll(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/categories?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).getAll(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/categories?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).getAll(ArgumentMatchers.any());
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGet() throws Exception {
            when(categoryService.getById(ArgumentMatchers.any())).thenReturn(categoryDto1);

            mvc.perform(get("/categories/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(categoryDto1)));

            verify(categoryService, times(1)).getById(ArgumentMatchers.any());
        }
    }
}
