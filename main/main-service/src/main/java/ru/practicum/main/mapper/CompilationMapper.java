package ru.practicum.main.mapper;

import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.EventState;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CompilationMapper {
    private CompilationMapper() {
    }

    public static CompilationDto toDto(Compilation compilation, Map<Long, Long> confirmedRequests,
                                       boolean publishedOnly) {
        Set<EventShortDto> events = compilation.getEvents().stream()
                .filter(event -> !publishedOnly || event.getState() == EventState.PUBLISHED)
                .map(event -> EventMapper.toShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toSet());
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}
