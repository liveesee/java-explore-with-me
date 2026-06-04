package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private ParticipationRequestRepository requestRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private ConfirmedRequestsService confirmedRequestsService;

    @InjectMocks
    private RequestService requestService;

    @Test
    void changeRequestStatus_withZeroLimitAndModerationEnabled_processesRequests() {
        Event event = event(0, true);
        ParticipationRequest request = pendingRequest(event);
        when(eventService.getUserEventOrThrow(10L, 1L)).thenReturn(event);
        when(requestRepository.findAllById(List.of(100L))).thenReturn(List.of(request));
        when(confirmedRequestsService.getConfirmedCount(1L)).thenReturn(0L);
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(
                10L, 1L, updateRequest("CONFIRMED", List.of(100L)));

        assertEquals(1, result.getConfirmedRequests().size());
    }

    @Test
    void changeRequestStatus_withModerationDisabled_skipsProcessing() {
        when(eventService.getUserEventOrThrow(10L, 1L)).thenReturn(event(0, false));

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(
                10L, 1L, updateRequest("CONFIRMED", List.of(100L)));

        assertEquals(0, result.getConfirmedRequests().size());
        verify(requestRepository, never()).findAllById(any());
    }

    @Test
    void changeRequestStatus_nonPendingRequest_throwsBadRequest() {
        Event event = event(10, true);
        ParticipationRequest request = pendingRequest(event);
        request.setStatus(RequestStatus.CONFIRMED);
        when(eventService.getUserEventOrThrow(10L, 1L)).thenReturn(event);
        when(requestRepository.findAllById(List.of(100L))).thenReturn(List.of(request));

        assertThrows(BadRequestException.class, () -> requestService.changeRequestStatus(
                10L, 1L, updateRequest("CONFIRMED", List.of(100L))));
    }

    @Test
    void createRequest_unpublishedEvent_throwsConflict() {
        Event event = event(0, true);
        event.setState(EventState.PENDING);
        when(userService.getUserOrThrow(20L)).thenReturn(User.builder().id(20L).build());
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> requestService.createRequest(20L, 1L));
    }

    @Test
    void createRequest_withoutModeration_autoConfirms() {
        Event event = event(0, false);
        when(userService.getUserOrThrow(20L)).thenReturn(User.builder().id(20L).name("U").email("u@t.c").build());
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(1L, 20L)).thenReturn(Optional.empty());
        when(confirmedRequestsService.getConfirmedCount(1L)).thenReturn(0L);
        when(requestRepository.save(any())).thenAnswer(invocation -> {
            ParticipationRequest saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        assertEquals("CONFIRMED", requestService.createRequest(20L, 1L).getStatus());
    }

    @Test
    void cancelRequest_whenNotOwner_throwsNotFound() {
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .requester(User.builder().id(99L).build())
                .event(event(0, true))
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(20L, 1L));
    }

    @Test
    void getUserRequests_returnsUserRequests() {
        User user = User.builder().id(20L).name("U").email("u@t.c").build();
        ParticipationRequest request = pendingRequest(event(0, true));
        when(userService.getUserOrThrow(20L)).thenReturn(user);
        when(requestRepository.findByRequesterId(20L)).thenReturn(List.of(request));

        assertEquals(1, requestService.getUserRequests(20L).size());
    }

    private Event event(int limit, boolean moderation) {
        return Event.builder()
                .id(1L)
                .state(EventState.PUBLISHED)
                .participantLimit(limit)
                .requestModeration(moderation)
                .initiator(User.builder().id(10L).build())
                .build();
    }

    private ParticipationRequest pendingRequest(Event event) {
        return ParticipationRequest.builder()
                .id(100L)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .event(event)
                .requester(User.builder().id(20L).build())
                .build();
    }

    private EventRequestStatusUpdateRequest updateRequest(String status, List<Long> ids) {
        return EventRequestStatusUpdateRequest.builder().status(status).requestIds(ids).build();
    }
}
