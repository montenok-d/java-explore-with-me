package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> findAllCommentsByUserId(Long userId, int from, int size);

    CommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto updateCommentByUser(long userId, long commentId, NewCommentDto newCommentDto);

    void deleteComment(long userId, long commentId);

    List<CommentDto> findCommentsByEvent(Long eventId, int from, int size);

    void deleteCommentByAdmin(long commentId);
}
