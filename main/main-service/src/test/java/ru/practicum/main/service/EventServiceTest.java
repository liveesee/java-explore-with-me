package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.dto.LocationDto;
import ru.practicum.main.dto.NewEventDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.dto.UpdateEventUserRequest;
import ru.practicum.main.exception.ForbiddenOperationException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.AdminStateAction;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventSort;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.User;
import ru.practicum.main.model.UserStateAction;
import ru.practicum.main.param.PublicEventSearchParams;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.main.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @Mock
    private StatsClient statsClient;
    @Mock
    private ConfirmedRequestsService confirmedRequestsService;

    @InjectMocks
    private EventService eventService;

    private final User user = User.builder().id(1L).name("User").email("u@t.c").build();
    private final Category category = Category.builder().id(2L).name("Cat").build();

    @Test
    void create_savesPendingEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(3);
        NewEventDto dto = NewEventDto.builder()
                .annotation("annotation with enough length for validation")
                .description("description with enough length for validation rules")
                .title("Title")
                .category(2L)
                .eventDate(DateTimeUtil.format(eventDate))
                .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                .build();
        Event saved = buildEvent(10L, EventState.PENDING, null);
        when(userService.getUserOrThrow(1L)).thenReturn(user);
        when(categoryService.getCategoryOrThrow(2L)).thenReturn(category);
        when(eventRepository.save(any())).thenReturn(saved);

        assertEquals("Title", eventService.create(1L, dto).getTitle());
    }

    @Test
    void updateUserEvent_whenPublished_throwsForbidden() {
        Event event = buildEvent(10L, EventState.PUBLISHED, null);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.updateUserEvent(
                1L, 10L, UpdateEventUserRequest.builder().title("New").build()));
    }

    @Test
    void updateUserEvent_cancelReview_setsCanceled() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(confirmedRequestsService.getConfirmedCount(10L)).thenReturn(0L);

        assertEquals("CANCELED", eventService.updateUserEvent(1L, 10L,
                UpdateEventUserRequest.builder().stateAction(UserStateAction.CANCEL_REVIEW.name()).build())
                .getState());
    }

    @Test
    void updateAdminEvent_publish_setsPublishedOn() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(confirmedRequestsService.getConfirmedCount(10L)).thenReturn(0L);

        assertEquals("PUBLISHED", eventService.updateAdminEvent(10L,
                UpdateEventAdminRequest.builder().stateAction(AdminStateAction.PUBLISH_EVENT.name()).build())
                .getState());
    }

    @Test
    void updateAdminEvent_publishWithEventDateTooSoon_throwsForbidden() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        event.setEventDate(LocalDateTime.now().plusMinutes(30));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.updateAdminEvent(10L,
                UpdateEventAdminRequest.builder().stateAction(AdminStateAction.PUBLISH_EVENT.name()).build()));
    }

    @Test
    void updateAdminEvent_rejectPublished_throwsForbidden() {
        Event event = buildEvent(10L, EventState.PUBLISHED, LocalDateTime.now());
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.updateAdminEvent(10L,
                UpdateEventAdminRequest.builder().stateAction(AdminStateAction.REJECT_EVENT.name()).build()));
    }

    @Test
    void getPublicEvent_whenNotPublished_throwsNotFound() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(NotFoundException.class, () -> eventService.getPublicEvent(10L));
    }

    @Test
    void getUserEventOrThrow_whenWrongUser_throwsNotFound() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(NotFoundException.class, () -> eventService.getUserEventOrThrow(99L, 10L));
    }

    @Test
    void getPublicEvents_withViewsSort_sortsByViews() {
        Event lowViews = buildEvent(1L, EventState.PUBLISHED, LocalDateTime.now());
        Event highViews = buildEvent(2L, EventState.PUBLISHED, LocalDateTime.now());
        when(eventRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(lowViews, highViews)));
        when(confirmedRequestsService.getConfirmedCounts(any())).thenReturn(Map.of());
        when(statsClient.getStats(any(), any(), any(), eq(true)))
                .thenReturn(List.of(
                        ViewStatsDto.builder().uri("/events/1").hits(1L).build(),
                        ViewStatsDto.builder().uri("/events/2").hits(100L).build()));

        var result = eventService.getPublicEvents(PublicEventSearchParams.builder()
                .sort(EventSort.VIEWS)
                .build());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void getUserEvents_returnsShortDtos() {
        Event event = buildEvent(10L, EventState.PENDING, null);
        when(userService.getUserOrThrow(1L)).thenReturn(user);
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(confirmedRequestsService.getConfirmedCounts(any())).thenReturn(Map.of());

        assertEquals(1, eventService.getUserEvents(1L, 0, 10).size());
        verify(statsClient, never()).getStats(any(), any(), any(), eq(true));
    }

    @Test
    void getEventsByIds_delegatesToRepository() {
        Event event = buildEvent(10L, EventState.PUBLISHED, LocalDateTime.now());
        when(eventRepository.findAllById(List.of(10L))).thenReturn(List.of(event));
        assertEquals(1, eventService.getEventsByIds(List.of(10L)).size());
    }

    private Event buildEvent(Long id, EventState state, LocalDateTime publishedOn) {
        return Event.builder()
                .id(id)
                .annotation("annotation with enough length for validation")
                .description("description with enough length for validation rules")
                .title("Title")
                .eventDate(LocalDateTime.now().plusDays(2))
                .createdOn(LocalDateTime.now())
                .publishedOn(publishedOn)
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
