package ru.practicum.main_service.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.comment.dto.CommentDto;
import ru.practicum.main_service.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> getCommentsByAdmin(Pageable pageable);

    void deleteByAdmin(Long commentId);

    List<CommentDto> getCommentsByPrivate(Long userId, Long eventId, Pageable pageable);

    CommentDto createByPrivate(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto patchByPrivate(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteByPrivate(Long userId, Long commentId);

    List<CommentDto> getCommentsByPublic(Long eventId, Pageable pageable);

    CommentDto getCommentByPublic(Long commentId);
}
