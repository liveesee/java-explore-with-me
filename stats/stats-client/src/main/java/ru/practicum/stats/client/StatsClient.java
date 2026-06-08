package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void hit(EndpointHitDto endpointHitDto);

    void hit(String uri, String ip);

    void hit(String uri, HttpServletRequest request);

    List<ViewStatsDto> getStats(StatsRequestDto request);

    default List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        return getStats(StatsRequestDto.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(unique)
                .build());
    }
}
