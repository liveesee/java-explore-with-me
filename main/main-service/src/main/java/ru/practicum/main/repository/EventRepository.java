package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.main.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    long countByCategoryId(Long categoryId);

    boolean existsByInitiatorId(Long initiatorId);

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
