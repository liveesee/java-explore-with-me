package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.repository.CommentRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentCountService {
    private final CommentRepository commentRepository;

    public Map<Long, Long> getCommentCounts(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentRepository.countByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.EventCommentCount::getEventId,
                        CommentRepository.EventCommentCount::getCount
                ));
    }

    public long getCommentCount(Long eventId) {
        return commentRepository.countByEventId(eventId);
    }
}
