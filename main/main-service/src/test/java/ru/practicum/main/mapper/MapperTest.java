package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {

    private final Category category = Category.builder().id(1L).name("Music").build();
    private final User user = User.builder().id(2L).name("Author").email("a@b.c").build();

    @Test
    void categoryMapper_mapsEntityAndDto() {
        NewCategoryDto newDto = NewCategoryDto.builder().name("Sport").build();
        Category entity = CategoryMapper.toEntity(newDto);
        assertEquals("Sport", entity.getName());
        assertEquals(1L, CategoryMapper.toDto(category).getId());
    }

    @Test
    void userMapper_mapsUser() {
        assertEquals("Author", UserMapper.toDto(user).getName());
        assertEquals(2L, UserMapper.toShortDto(user).getId());
    }

    @Test
    void eventMapper_mapsEventWithStats() {
        Event event = buildEvent(EventState.PUBLISHED);
        assertEquals(5L, EventMapper.toShortDto(event, 3L, 5L).getViews());
        assertEquals("PUBLISHED", EventMapper.toFullDto(event, 3L, null).getState());
        assertNull(EventMapper.toLocationDto(Event.builder().id(1L).build()));
    }

    @Test
    void eventMapper_getViewsFromMap() {
        assertEquals(10L, EventMapper.getViews(Map.of(1L, 10L), 1L));
        assertEquals(0L, EventMapper.getViews(Map.of(), 99L));
    }

    @Test
    void requestMapper_mapsRequest() {
        Event event = buildEvent(EventState.PUBLISHED);
        ParticipationRequest request = ParticipationRequest.builder()
                .id(5L)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .event(event)
                .requester(user)
                .build();
        assertEquals(5L, RequestMapper.toDto(request).getId());
        assertEquals("PENDING", RequestMapper.toDto(request).getStatus());
    }

    @Test
    void compilationMapper_filtersUnpublishedWhenRequired() {
        Event published = buildEvent(EventState.PUBLISHED);
        Event pending = buildEvent(EventState.PENDING);
        pending.setId(99L);
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Top")
                .pinned(true)
                .events(Set.of(published, pending))
                .build();
        assertEquals(1, CompilationMapper.toDto(compilation, Map.of(), true).getEvents().size());
        assertEquals(2, CompilationMapper.toDto(compilation, Map.of(), false).getEvents().size());
    }

    private Event buildEvent(EventState state) {
        return Event.builder()
                .id(10L)
                .annotation("annotation with enough length for validation")
                .description("description with enough length for validation rules")
                .title("Title")
                .eventDate(LocalDateTime.now().plusDays(2))
                .createdOn(LocalDateTime.now())
                .state(state)
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .location(new Location(55.75, 37.62))
                .category(category)
                .initiator(user)
                .build();
    }
}
