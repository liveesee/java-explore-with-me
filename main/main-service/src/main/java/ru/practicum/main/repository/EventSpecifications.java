package ru.practicum.main.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class EventSpecifications {
    private EventSpecifications() {
    }

    public static Specification<Event> adminFilter(Collection<Long> users, Collection<EventState> states,
                                                   Collection<Long> categories, LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> publicFilter(String text, Collection<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                    Boolean onlyAvailable) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));
            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }
            if (rangeStart != null && rangeEnd != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            } else if (rangeStart == null && rangeEnd == null) {
                predicates.add(cb.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            } else if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            } else {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            if (Boolean.TRUE.equals(onlyAvailable)) {
                var subquery = query.subquery(Long.class);
                var requestRoot = subquery.from(ru.practicum.main.model.ParticipationRequest.class);
                subquery.select(cb.count(requestRoot));
                subquery.where(
                        cb.equal(requestRoot.get("event"), root),
                        cb.equal(requestRoot.get("status"), ru.practicum.main.model.RequestStatus.CONFIRMED)
                );
                predicates.add(cb.or(
                        cb.equal(root.get("participantLimit"), 0),
                        cb.lessThan(subquery, root.get("participantLimit"))
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
