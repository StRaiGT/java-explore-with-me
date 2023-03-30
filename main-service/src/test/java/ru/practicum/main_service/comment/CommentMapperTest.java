package ru.practicum.main_service.comment;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.comment.dto.CommentDto;
import ru.practicum.main_service.comment.mapper.CommentMapperImpl;
import ru.practicum.main_service.comment.model.Comment;
import ru.practicum.main_service.event.model.Event;
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
public class CommentMapperTest {
    @Mock
    private UserMapperImpl userMapper;

    @InjectMocks
    private CommentMapperImpl commentMapper;

    private final User user = User.builder()
            .id(1L)
            .name("test name")
            .email("test@yandex.ru")
            .build();
    private final UserShortDto userShortDto = UserShortDto.builder()
            .id(user.getId())
            .name(user.getName())
            .build();
    private final Event event = Event.builder()
            .id(1L)
            .build();
    private final Comment comment = Comment.builder()
            .id(1L)
            .author(user)
            .event(event)
            .createdOn(LocalDateTime.now())
            .editedOn(LocalDateTime.now().plusHours(1))
            .build();
    private final CommentDto commentDto = CommentDto.builder()
            .id(comment.getId())
            .text(comment.getText())
            .author(userShortDto)
            .eventId(event.getId())
            .createdOn(comment.getCreatedOn())
            .editedOn(comment.getEditedOn())
            .build();

    @Nested
    class ToCommentDto {
        @Test
        public void shouldReturnCommentDto() {
            when(userMapper.toUserShortDto(any())).thenCallRealMethod();

            CommentDto result = commentMapper.toCommentDto(comment);

            assertEquals(commentDto.getId(), result.getId());
            assertEquals(commentDto.getText(), result.getText());
            assertEquals(commentDto.getAuthor().getId(), result.getAuthor().getId());
            assertEquals(commentDto.getEventId(), result.getEventId());
            assertEquals(commentDto.getCreatedOn(), result.getCreatedOn());
            assertEquals(commentDto.getEditedOn(), result.getEditedOn());

            verify(userMapper, times(1)).toUserShortDto(any());
        }

        @Test
        public void shouldReturnNull() {
            CommentDto result = commentMapper.toCommentDto(null);

            assertNull(result);
        }
    }
}
