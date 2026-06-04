package ru.practicum.main.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.main.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private static final String APP_NAME = "ewm-main-service";
    private static final LocalDateTime STATS_START = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime STATS_END = LocalDateTime.of(2035, 5, 5, 0, 0);

    private final StatsClient statsClient;

    public void hit(String uri) {
        hit(uri, resolveIpFromContext());
    }

    public void hit(String uri, HttpServletRequest request) {
        hit(uri, resolveIp(request));
    }

    public void hit(String uri, String ip) {
        statsClient.hit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build());
    }

    public Map<Long, Long> getViews(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());
        List<ViewStatsDto> stats;
        try {
            stats = statsClient.getStats(STATS_START, STATS_END, uris, true);
        } catch (RestClientException exception) {
            log.warn("Failed to get views for events {}", eventIds, exception);
            return Collections.emptyMap();
        }
        return mapViews(stats);
    }

    public Long getView(Long eventId) {
        return getViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    private Map<Long, Long> mapViews(List<ViewStatsDto> stats) {
        if (stats == null || stats.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> views = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
            views.put(eventId, stat.getHits());
        }
        return views;
    }

    private String resolveIpFromContext() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
        return resolveIp(attributes.getRequest());
    }

    static String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
