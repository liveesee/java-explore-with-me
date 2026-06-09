package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.main.dto.CommentRequestDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentRepository;
import ru.practicum.main.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_onUnpublishedEvent_throwsConflict() {
        User user = User.builder().id(1L).name("User").build();
        Event event = Event.builder().id(10L).state(EventState.PENDING).build();
        when(userService.getUserOrThrow(1L)).thenReturn(user);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> commentService.create(1L, 10L,
                CommentRequestDto.builder().text("comment").build()));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_onMissingEvent_throwsNotFound() {
        User user = User.builder().id(1L).name("User").build();
        when(userService.getUserOrThrow(1L)).thenReturn(user);
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.create(1L, 10L,
                CommentRequestDto.builder().text("comment").build()));
    }

    @Test
    void update_byNonAuthor_throwsConflict() {
        User author = User.builder().id(1L).name("Author").build();
        Event event = Event.builder().id(10L).state(EventState.PUBLISHED).build();
        Comment comment = Comment.builder()
                .id(100L)
                .text("old")
                .created(LocalDateTime.now())
                .author(author)
                .event(event)
                .build();
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThrows(ConflictException.class, () -> commentService.update(2L, 100L,
                CommentRequestDto.builder().text("new").build()));
    }

    @Test
    void getByEventId_onUnpublishedEvent_throwsNotFound() {
        Event event = Event.builder().id(10L).state(EventState.PENDING).build();
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(NotFoundException.class, () -> commentService.getByEventId(10L, 0, 10));
    }

    @Test
    void getById_onUnpublishedEvent_throwsNotFound() {
        User author = User.builder().id(1L).name("Author").build();
        Event event = Event.builder().id(10L).state(EventState.PENDING).build();
        Comment comment = Comment.builder()
                .id(100L)
                .text("text")
                .created(LocalDateTime.now())
                .author(author)
                .event(event)
                .build();
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThrows(NotFoundException.class, () -> commentService.getById(100L));
    }

    @Test
    void getUserComments_returnsList() {
        User user = User.builder().id(1L).name("Author").build();
        Event event = Event.builder().id(10L).state(EventState.PUBLISHED).build();
        Comment comment = Comment.builder()
                .id(100L)
                .text("text")
                .created(LocalDateTime.now())
                .author(user)
                .event(event)
                .build();
        when(userService.getUserOrThrow(1L)).thenReturn(user);
        when(commentRepository.findByAuthorId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        assertEquals(1, commentService.getUserComments(1L, null, 0, 10).size());
    }
}
