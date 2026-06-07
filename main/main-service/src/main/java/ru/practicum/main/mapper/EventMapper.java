package ru.practicum.main.mapper;

import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.LocationDto;
import ru.practicum.main.model.Event;

import java.util.Map;

public final class EventMapper {
    private EventMapper() {
    }

    public static EventShortDto toShortDto(Event event, Long confirmedRequests) {
        return toShortDto(event, confirmedRequests, 0L);
    }

    public static EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views != null ? views : 0L)
                .build();
    }

    public static EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .location(toLocationDto(event))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views != null ? views : 0L)
                .build();
    }

    public static LocationDto toLocationDto(Event event) {
        if (event.getLocation() == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(event.getLocation().getLat().floatValue())
                .lon(event.getLocation().getLon().floatValue())
                .build();
    }

    public static Long getViews(Map<Long, Long> viewsMap, Long eventId) {
        return viewsMap.getOrDefault(eventId, 0L);
    }
}
