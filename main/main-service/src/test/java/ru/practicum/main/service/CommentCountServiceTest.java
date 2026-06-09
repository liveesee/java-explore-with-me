package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.repository.CommentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentCountServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentCountService commentCountService;

    @Test
    void getCommentCount_returnsCount() {
        when(commentRepository.countByEventId(10L)).thenReturn(5L);
        assertEquals(5L, commentCountService.getCommentCount(10L));
    }

    @Test
    void getCommentCounts_withEmptyIds_returnsEmptyMap() {
        assertTrue(commentCountService.getCommentCounts(Collections.emptyList()).isEmpty());
    }

    @Test
    void getCommentCounts_returnsMap() {
        when(commentRepository.countByEventIds(List.of(10L, 20L)))
                .thenReturn(List.of(
                        new EventCommentCountStub(10L, 3L),
                        new EventCommentCountStub(20L, 7L)
                ));

        Map<Long, Long> counts = commentCountService.getCommentCounts(List.of(10L, 20L));

        assertEquals(3L, counts.get(10L));
        assertEquals(7L, counts.get(20L));
    }

    private static final class EventCommentCountStub implements CommentRepository.EventCommentCount {
        private final Long eventId;
        private final Long count;

        private EventCommentCountStub(Long eventId, Long count) {
            this.eventId = eventId;
            this.count = count;
        }

        @Override
        public Long getEventId() {
            return eventId;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }
}
