package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.NewEventDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.dto.UpdateEventUserRequest;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ForbiddenOperationException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.AdminStateAction;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventSort;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.User;
import ru.practicum.main.model.UserStateAction;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.EventSpecifications;
import ru.practicum.main.stats.StatsService;
import ru.practicum.main.util.DateTimeUtil;
import ru.practicum.main.util.EnumUtil;
import ru.practicum.main.util.PageUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final StatsService statsService;
    private final ConfirmedRequestsService confirmedRequestsService;

    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User initiator = userService.getUserOrThrow(userId);
        Category category = categoryService.getCategoryOrThrow(dto.getCategory());
        LocalDateTime eventDate = DateTimeUtil.parse(dto.getEventDate());
        DateTimeUtil.validateEventDateForUser(eventDate);
        Event event = Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .eventDate(eventDate)
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .location(toLocation(dto.getLocation()))
                .category(category)
                .initiator(initiator)
                .build();
        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L, 0L);
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userService.getUserOrThrow(userId);
        Pageable pageable = PageUtil.createPageable(from, size);
        Page<Event> page = eventRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("initiator").get("id"), userId),
                pageable
        );
        return toShortDtoList(page.getContent());
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = getUserEventOrThrow(userId, eventId);
        return toFullDto(event);
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = getUserEventOrThrow(userId, eventId);
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ForbiddenOperationException("Only pending or canceled events can be changed");
        }
        applyUserUpdate(event, dto);
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start = rangeStart != null ? DateTimeUtil.parse(rangeStart) : null;
        LocalDateTime end = rangeEnd != null ? DateTimeUtil.parse(rangeEnd) : null;
        Pageable pageable = PageUtil.createPageable(from, size);
        Specification<Event> spec = EventSpecifications.adminFilter(users, states, categories, start, end);
        return eventRepository.findAll(spec, pageable).stream()
                .map(this::toFullDto)
                .toList();
    }

    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        applyAdminUpdate(event, dto);
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                               EventSort sort, int from, int size) {
        LocalDateTime start = rangeStart != null ? DateTimeUtil.parse(rangeStart) : null;
        LocalDateTime end = rangeEnd != null ? DateTimeUtil.parse(rangeEnd) : null;
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Range end must be after range start");
        }
        Specification<Event> spec = EventSpecifications.publicFilter(text, categories, paid, start, end, onlyAvailable);

        if (sort == EventSort.VIEWS) {
            List<Event> events = eventRepository.findAll(spec, Pageable.unpaged()).getContent();
            List<EventShortDto> dtos = toShortDtoList(events);
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            return paginateList(dtos, from, size);
        }

        Pageable pageable = PageUtil.createPageable(from, size, Sort.by("eventDate").ascending());
        Page<Event> page = eventRepository.findAll(spec, pageable);
        return toShortDtoList(page.getContent());
    }

    public EventFullDto getPublicEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return EventMapper.toFullDto(
                event,
                confirmedRequestsService.getConfirmedCount(eventId),
                statsService.getViewsAfterHit(eventId));
    }

    public Event getPublishedEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    public Event getUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findById(eventId)
                .filter(event -> event.getInitiator().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    public List<Event> getEventsByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    private void applyUserUpdate(Event event, UpdateEventUserRequest dto) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryOrThrow(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = DateTimeUtil.parse(dto.getEventDate());
            DateTimeUtil.validateEventDateForUser(eventDate);
            event.setEventDate(eventDate);
        }
        if (dto.getLocation() != null) {
            event.setLocation(toLocation(dto.getLocation()));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getStateAction() != null) {
            UserStateAction action = EnumUtil.parse(UserStateAction.class, dto.getStateAction());
            if (action == UserStateAction.SEND_TO_REVIEW) {
                DateTimeUtil.validateEventDateForUser(event.getEventDate());
                event.setState(EventState.PENDING);
            } else if (action == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void applyAdminUpdate(Event event, UpdateEventAdminRequest dto) {
        if (dto.getStateAction() != null) {
            AdminStateAction action = EnumUtil.parse(AdminStateAction.class, dto.getStateAction());
            if (action == AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ForbiddenOperationException(
                            "Cannot publish the event because it's not in the right state: " + event.getState());
                }
                LocalDateTime publishedOn = LocalDateTime.now();
                DateTimeUtil.validateEventDateForAdmin(event.getEventDate(), publishedOn);
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(publishedOn);
            } else if (action == AdminStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ForbiddenOperationException(
                            "Cannot reject the event because it's already published");
                }
                event.setState(EventState.CANCELED);
            }
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryOrThrow(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = DateTimeUtil.parse(dto.getEventDate());
            DateTimeUtil.validateEventDateNotInPast(eventDate);
            DateTimeUtil.validateEventDateForAdmin(eventDate, event.getPublishedOn());
            event.setEventDate(eventDate);
        }
        if (dto.getLocation() != null) {
            event.setLocation(toLocation(dto.getLocation()));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
    }

    private List<EventShortDto> toShortDtoList(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = confirmedRequestsService.getConfirmedCounts(ids);
        Map<Long, Long> views = statsService.getViews(ids);
        return events.stream()
                .map(event -> EventMapper.toShortDto(
                        event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private EventFullDto toFullDto(Event event) {
        return EventMapper.toFullDto(
                event,
                confirmedRequestsService.getConfirmedCount(event.getId()),
                statsService.getView(event.getId())
        );
    }

    private Location toLocation(ru.practicum.main.dto.LocationDto dto) {
        return new Location(dto.getLat().doubleValue(), dto.getLon().doubleValue());
    }

    private <T> List<T> paginateList(List<T> list, int from, int size) {
        int toIndex = Math.min(from + size, list.size());
        if (from >= list.size()) {
            return List.of();
        }
        return list.subList(from, toIndex);
    }
}
