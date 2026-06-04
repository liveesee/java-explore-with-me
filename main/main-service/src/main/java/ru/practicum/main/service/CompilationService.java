package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.CompilationSpecifications;
import ru.practicum.main.stats.StatsService;
import ru.practicum.main.util.PageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;
    private final ConfirmedRequestsService confirmedRequestsService;
    private final StatsService statsService;

    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(resolveEvents(dto.getEvents()))
                .build();
        return toDto(compilationRepository.save(compilation), false);
    }

    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilationOrThrow(compId);
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            compilation.setEvents(resolveEvents(dto.getEvents()));
        }
        return toDto(compilationRepository.save(compilation), false);
    }

    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageUtil.createPageable(from, size);
        Specification<Compilation> spec = CompilationSpecifications.byPinned(pinned);
        Page<Compilation> page = compilationRepository.findAll(spec, pageable);
        return page.getContent().stream()
                .map(compilation -> toDto(compilation, true))
                .toList();
    }

    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findByIdWithEvents(compId);
        if (compilation == null) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        return toDto(compilation, true);
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private Set<Event> resolveEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> ids = eventIds.stream().toList();
        List<Event> events = eventService.getEventsByIds(ids);
        if (events.size() != ids.size()) {
            throw new NotFoundException("Event was not found");
        }
        return new HashSet<>(events);
    }

    private CompilationDto toDto(Compilation compilation, boolean publishedOnly) {
        List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = confirmedRequestsService.getConfirmedCounts(eventIds);
        Map<Long, Long> views = statsService.getViews(eventIds);
        return CompilationMapper.toDto(compilation, confirmed, views, publishedOnly);
    }
}
