package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CompilationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {

    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private EventService eventService;
    @Mock
    private ConfirmedRequestsService confirmedRequestsService;

    @InjectMocks
    private CompilationService compilationService;

    @Test
    void create_savesCompilation() {
        NewCompilationDto dto = NewCompilationDto.builder().title("Best").pinned(true).build();
        Compilation saved = Compilation.builder().id(1L).title("Best").pinned(true).events(Set.of()).build();
        when(compilationRepository.save(any())).thenReturn(saved);
        when(confirmedRequestsService.getConfirmedCounts(any())).thenReturn(Map.of());

        assertEquals("Best", compilationService.create(dto).getTitle());
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(compilationRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> compilationService.delete(1L));
    }

    @Test
    void delete_whenExists_deletes() {
        when(compilationRepository.existsById(1L)).thenReturn(true);
        compilationService.delete(1L);
        verify(compilationRepository).deleteById(1L);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(compilationRepository.findByIdWithEvents(1L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> compilationService.getById(1L));
    }

    @Test
    void getAll_returnsCompilations() {
        Compilation compilation = Compilation.builder().id(1L).title("Top").pinned(false).build();
        when(compilationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(compilation)));
        when(confirmedRequestsService.getConfirmedCounts(any())).thenReturn(Map.of());

        assertEquals(1, compilationService.getAll(null, 0, 10).size());
        verify(confirmedRequestsService).getConfirmedCounts(any());
    }

    @Test
    void create_whenEventMissing_throwsNotFound() {
        NewCompilationDto dto = NewCompilationDto.builder().title("Best").events(Set.of(99L)).build();
        when(eventService.getEventsByIds(List.of(99L))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> compilationService.create(dto));
    }

    @Test
    void update_changesFields() {
        Compilation compilation = Compilation.builder().id(1L).title("Old").pinned(false).build();
        Event event = buildEvent();
        when(compilationRepository.findById(1L)).thenReturn(java.util.Optional.of(compilation));
        when(eventService.getEventsByIds(List.of(10L))).thenReturn(List.of(event));
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(confirmedRequestsService.getConfirmedCounts(any())).thenReturn(Map.of());

        assertEquals("New", compilationService.update(1L,
                UpdateCompilationRequest.builder().title("New").events(Set.of(10L)).build()).getTitle());
    }

    private Event buildEvent() {
        User user = User.builder().id(1L).name("U").email("u@t.c").build();
        Category category = Category.builder().id(2L).name("C").build();
        return Event.builder()
                .id(10L)
                .annotation("annotation with enough length for validation")
                .description("description with enough length for validation rules")
                .title("Title")
                .eventDate(LocalDateTime.now().plusDays(2))
                .createdOn(LocalDateTime.now())
                .state(EventState.PUBLISHED)
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .location(new Location(55.75, 37.62))
                .category(category)
                .initiator(user)
                .build();
    }
}
