package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.ViewStatsProjection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class StatsMapper {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DATE_FORMAT_MESSAGE = "Invalid date format. Expected: yyyy-MM-dd HH:mm:ss";

    private StatsMapper() {
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(DATE_FORMAT_MESSAGE);
        }
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(parseDateTime(dto.getTimestamp()))
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
