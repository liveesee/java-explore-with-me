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

    private final StatsClient statsClient;

    public void hit(String uri) {
        hit(uri, LocalDateTime.now());
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
            stats = statsClient.getStats(STATS_START, LocalDateTime.now().plusYears(1), uris, true);
        } catch (RestClientException exception) {
            log.warn("Failed to get views for events {}", eventIds, exception);
            return Collections.emptyMap();
        }
        if (stats == null) {
            return Collections.emptyMap();
        }
        Map<Long, Long> views = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
            views.put(eventId, stat.getHits());
        }
        return views;
    }

    public Long getView(Long eventId) {
        return getViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    public Long getViewsAfterHit(Long eventId) {
        String uri = "/events/" + eventId;
        LocalDateTime timestamp = LocalDateTime.now();
        hit(uri, timestamp);
        return getViewForUri(uri, timestamp);
    }

    private void hit(String uri, LocalDateTime timestamp) {
        try {
            statsClient.hit(EndpointHitDto.builder()
                    .app(APP_NAME)
                    .uri(uri)
                    .ip(getClientIp())
                    .timestamp(DateTimeUtil.format(timestamp))
                    .build());
        } catch (RestClientException exception) {
            log.warn("Failed to save hit for uri={}", uri, exception);
        }
    }

    private Long getViewForUri(String uri, LocalDateTime hitTime) {
        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    STATS_START, hitTime.plusSeconds(1), List.of(uri), true);
            if (stats == null || stats.isEmpty()) {
                return 0L;
            }
            return stats.get(0).getHits();
        } catch (RestClientException exception) {
            log.warn("Failed to get views for uri={}", uri, exception);
            return 0L;
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "0.0.0.0";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
