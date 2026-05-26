package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String serverUrl;

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
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String url = buildStatsUrl(start, end, uris, unique);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }
        ).getBody();
    }

    private String buildStatsUrl(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder url = new StringBuilder(serverUrl);
        url.append("/stats?start=").append(encode(FORMATTER.format(start)));
        url.append("&end=").append(encode(FORMATTER.format(end)));
        if (uris != null) {
            for (String uri : uris) {
                url.append("&uris=").append(encode(uri));
            }
        }
        url.append("&unique=").append(unique);
        return url.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
