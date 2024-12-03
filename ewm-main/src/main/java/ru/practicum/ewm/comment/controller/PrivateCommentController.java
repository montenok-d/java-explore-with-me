package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateCommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public List<CommentDto> findAllByUserId(@PathVariable("userId") Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        log.info("GET:/users/{}/comments; from: {}, size: {}.", userId, from, size);
        return commentService.findAllCommentsByUserId(userId, from, size);
    }

    @PostMapping("events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable("userId") Long userId,
                                    @PathVariable("eventId") Long eventId) {
        log.info("POST:/users/{}/events/{}/comments; newCommentDto: {}.", userId, eventId, newCommentDto);
        return commentService.createComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateCommentByUser(@PathVariable long userId,
                                          @PathVariable long commentId,
                                          @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("PATCH:/users/{}/comments/{}; newCommentDto: {}. ", userId, commentId, newCommentDto);
        return commentService.updateCommentByUser(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("userId") long userId,
                              @PathVariable("commentId") long commentId) {
        log.info("DELETE:/users/{}/comments/{}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }
}
