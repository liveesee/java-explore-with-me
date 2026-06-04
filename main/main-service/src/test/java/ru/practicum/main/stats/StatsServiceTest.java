package ru.practicum.main.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getViews_emptyIds_returnsEmptyMap() {
        assertTrue(statsService.getViews(Collections.emptyList()).isEmpty());
        assertTrue(statsService.getViews(null).isEmpty());
    }

    @Test
    void getViews_mapsStatsByEventId() {
        ViewStatsDto stat = ViewStatsDto.builder().uri("/events/42").hits(7L).build();
        when(statsClient.getStats(any(), any(), anyList(), eq(true))).thenReturn(List.of(stat));

        Map<Long, Long> views = statsService.getViews(List.of(42L));
        assertEquals(7L, views.get(42L));
    }

    @Test
    void getView_returnsZeroWhenMissing() {
        when(statsClient.getStats(any(), any(), anyList(), eq(true))).thenReturn(List.of());
        assertEquals(0L, statsService.getView(1L));
    }

    @Test
    void hit_sendsEndpointToClient() {
        statsService.hit("/events");
        verify(statsClient).hit(any(EndpointHitDto.class));
    }
}
