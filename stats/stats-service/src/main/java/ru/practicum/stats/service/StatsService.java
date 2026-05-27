package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.repository.EndpointHitRepository;
import ru.practicum.stats.repository.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final EndpointHitRepository repository;

    @Transactional
    public void saveHit(EndpointHitDto dto) {
        repository.save(StatsMapper.toEntity(dto));
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<String> urisParam = uris == null || uris.isEmpty() ? null : uris;
        List<ViewStatsProjection> stats = unique
                ? repository.findUniqueStats(start, end, urisParam)
                : repository.findStats(start, end, urisParam);
        return stats.stream().map(StatsMapper::toDto).toList();
    }
}
