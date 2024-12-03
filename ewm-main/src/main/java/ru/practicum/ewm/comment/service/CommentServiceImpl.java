package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentDto> findAllCommentsByUserId(Long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found"));
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("createdOn").descending());
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageRequest);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id: " + eventId + "not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found"));
        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Initiator cannot comment on their event.");
        }
        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setEvent(event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(long userId, long commentId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment id: " + commentId + " not found"));
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ValidationException("Only comment's author can edit their comment.");
        }
        comment.setText(newCommentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment id: " + commentId + " not found"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException("Only comment's author can delete their comment.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> findCommentsByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id: " + eventId + "not found."));
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("createdOn").descending());
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageRequest);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment id: " + commentId + " not found"));
        commentRepository.deleteById(commentId);
    }
}
