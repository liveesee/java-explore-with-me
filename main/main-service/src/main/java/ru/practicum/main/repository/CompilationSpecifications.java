package ru.practicum.main.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.model.Compilation;

public final class CompilationSpecifications {
    private CompilationSpecifications() {
    }

    public static Specification<Compilation> byPinned(Boolean pinned) {
        return (root, query, cb) -> {
            if (pinned == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("pinned"), pinned);
        };
    }
}
