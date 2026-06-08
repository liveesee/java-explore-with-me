package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, JpaSpecificationExecutor<Compilation> {
    @Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events e "
            + "LEFT JOIN FETCH e.category LEFT JOIN FETCH e.initiator WHERE c.id = :id")
    Compilation findByIdWithEvents(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findAll(Specification<Compilation> spec, Pageable pageable);
}
