package ru.practicum.main_service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main_service.user.controller.UserAdminController;
import ru.practicum.main_service.user.dto.NewUserRequest;
import ru.practicum.main_service.user.dto.UserDto;
import ru.practicum.main_service.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAdminController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserAdminControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private UserService userService;

    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .email("test1@yandex.ru")
            .name("test name 1")
            .build();
    private final UserDto userDto2 = UserDto.builder()
            .id(2L)
            .email("test2@yandex.ru")
            .name("test name 2")
            .build();
    private NewUserRequest newUserRequest;

    @Nested
    class Create {
        @BeforeEach
        public void beforeEach() {
            newUserRequest = NewUserRequest.builder()
                    .email("test1@yandex.ru")
                    .name("test name 1")
                    .build();
        }

        @Test
        public void shouldCreate() throws Exception {
            when(userService.create(ArgumentMatchers.any())).thenReturn(userDto1);

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(userDto1)));

            verify(userService, times(1)).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfNameIsNull() throws Exception {
            newUserRequest.setName(null);

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfNameIsEmpty() throws Exception {
            newUserRequest.setName("");

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfNameIsBlank() throws Exception {
            newUserRequest.setName(" ");

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfEmailIsNull() throws Exception {
            newUserRequest.setEmail(null);

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfEmailIsEmpty() throws Exception {
            newUserRequest.setEmail("");

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfEmailIsBlank() throws Exception {
            newUserRequest.setEmail(" ");

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfEmailIsNotValid() throws Exception {
            newUserRequest.setEmail("testYandex.ru");

            mvc.perform(post("/admin/users")
                            .content(mapper.writeValueAsString(newUserRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).create(ArgumentMatchers.any());
        }
    }

    @Nested
    class GetUsers {
        @Test
        public void shouldGet() throws Exception {
            when(userService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(List.of(userDto1));

            mvc.perform(get("/admin/users?ids=1&from=0&size=5")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(userDto1))));

            verify(userService, times(1)).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldGetAllIfIdsEmpty() throws Exception {
            when(userService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(List.of(userDto1, userDto2));

            mvc.perform(get("/admin/users?ids=&from=0&size=5")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(userDto1, userDto2))));

            verify(userService, times(1)).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldGetAllByDefault() throws Exception {
            when(userService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(List.of(userDto1, userDto2));

            mvc.perform(get("/admin/users")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(userDto1, userDto2))));

            verify(userService, times(1)).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldGetEmptyByDefault() throws Exception {
            when(userService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(List.of());

            mvc.perform(get("/admin/users")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(userService, times(1)).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldGetEmptyIfIdIsNegative() throws Exception {
            when(userService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(List.of());

            mvc.perform(get("/admin/users?ids=-1&from=0&size=5")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));

            verify(userService, times(1)).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfFromIsNegative() throws Exception {
            mvc.perform(get("/admin/users?from=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsZero() throws Exception {
            mvc.perform(get("/admin/users?size=0")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        public void shouldReturnBadRequestIfSizeIsNegative() throws Exception {
            mvc.perform(get("/admin/users?size=-1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    class DeleteById {
        @Test
        public void shouldDelete() throws Exception {
            mvc.perform(delete("/admin/users/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteById(ArgumentMatchers.any());
        }
    }
}
