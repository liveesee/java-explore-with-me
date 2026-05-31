package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.ViewStatsProjection;

import java.time.LocalDateTime;

public final class StatsMapper {
    private StatsMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto, LocalDateTime timestamp) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(timestamp)
                .build();
    }

    public static ViewStatsDto toDto(ViewStatsProjection projection) {
        return ViewStatsDto.builder()
                .app(projection.getApp())
                .uri(projection.getUri())
                .hits(projection.getHits())
                .build();
    }
}
