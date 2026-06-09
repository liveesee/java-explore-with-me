package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Comment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Override
    @EntityGraph(attributePaths = {"author", "event"})
    Optional<Comment> findById(Long id);

    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findByAuthorIdAndEventId(Long authorId, Long eventId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findAll(Pageable pageable);

    boolean existsByAuthorId(Long authorId);

    long countByEventId(Long eventId);

    @Query("SELECT c.event.id AS eventId, COUNT(c) AS count FROM Comment c "
            + "WHERE c.event.id IN :eventIds GROUP BY c.event.id")
    List<EventCommentCount> countByEventIds(@Param("eventIds") Collection<Long> eventIds);

    interface EventCommentCount {
        Long getEventId();

        Long getCount();
    }
}
