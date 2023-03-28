package ru.practicum.main_service.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main_service.comment.dto.CommentDto;
import ru.practicum.main_service.comment.dto.NewCommentDto;
import ru.practicum.main_service.comment.model.Comment;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.user.mapper.UserMapper;
import ru.practicum.main_service.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "event", expression = "java(event)")
    @Mapping(target = "createdOn", expression = "java(createdOn)")
    @Mapping(target = "editedOn", expression = "java(null)")
    Comment toComment(NewCommentDto newCommentDto, User author, Event event, LocalDateTime createdOn);

    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    CommentDto toCommentDto(Comment comment);
}
