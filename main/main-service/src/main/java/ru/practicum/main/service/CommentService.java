package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.CommentRequestDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.util.PageUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private static final Sort CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Transactional
    public CommentDto create(Long userId, Long eventId, CommentRequestDto dto) {
        User author = userService.getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }
        Comment comment = CommentMapper.toEntity(dto.getText(), author, event);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto update(Long userId, Long commentId, CommentRequestDto dto) {
        Comment comment = getCommentOrThrow(commentId);
        checkAuthor(comment, userId);
        CommentMapper.applyUpdate(comment, dto.getText());
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteByUser(Long userId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        checkAuthor(comment, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteByAdmin(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        commentRepository.delete(comment);
    }

    public List<CommentDto> getByEventId(Long eventId, int from, int size) {
        Event event = getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Pageable pageable = PageUtil.createPageable(from, size, CREATED_DESC);
        return commentRepository.findByEventId(eventId, pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    public CommentDto getById(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        if (comment.getEvent().getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }
        return CommentMapper.toDto(comment);
    }

    public List<CommentDto> getUserComments(Long userId, Long eventId, int from, int size) {
        userService.getUserOrThrow(userId);
        Pageable pageable = PageUtil.createPageable(from, size, CREATED_DESC);
        if (eventId != null) {
            return commentRepository.findByAuthorIdAndEventId(userId, eventId, pageable).stream()
                    .map(CommentMapper::toDto)
                    .toList();
        }
        return commentRepository.findByAuthorId(userId, pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    public List<CommentDto> getAllComments(int from, int size) {
        Pageable pageable = PageUtil.createPageable(from, size, CREATED_DESC);
        return commentRepository.findAll(pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkAuthor(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Only the author can edit or delete the comment");
        }
    }
}
