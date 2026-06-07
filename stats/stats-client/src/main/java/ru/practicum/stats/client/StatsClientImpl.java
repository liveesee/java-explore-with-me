package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String serverUrl;
    private final String appName;

    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(
                serverUrl + "/hit",
                new HttpEntity<>(endpointHitDto, headers),
                Void.class
        );
    }

    @Override
    public void hit(String uri, String ip) {
        hit(EndpointHitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(FORMATTER.format(LocalDateTime.now()))
                .build());
    }

    @Override
    public void hit(String uri, HttpServletRequest request) {
        hit(uri, resolveIp(request));
    }

    @Override
    public List<ViewStatsDto> getStats(StatsRequestDto request) {
        String url = buildStatsUrl(request);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }
        ).getBody();
    }

    private String buildStatsUrl(StatsRequestDto request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", FORMATTER.format(request.getStart()))
                .queryParam("end", FORMATTER.format(request.getEnd()))
                .queryParam("unique", request.isUnique());
        List<String> uris = request.getUris();
        if (uris != null) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }
        return builder.encode().build().toUriString();
    }

    private static String resolveIp(HttpServletRequest request) {
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
