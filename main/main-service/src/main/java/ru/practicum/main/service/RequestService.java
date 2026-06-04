package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.model.User;
import ru.practicum.main.util.EnumUtil;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final UserService userService;
    private final ConfirmedRequestsService confirmedRequestsService;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getUserOrThrow(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userService.getUserOrThrow(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The initiator cannot add a request to participate in his event");
        }
        long confirmed = confirmedRequestsService.getConfirmedCount(eventId);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }
        RequestStatus status = resolveInitialRequestStatus(event);

        Optional<ParticipationRequest> existing = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (existing.isPresent()) {
            ParticipationRequest request = existing.get();
            if (request.getStatus() != RequestStatus.CANCELED) {
                throw new ConflictException("Request already exists");
            }
            request.setCreated(LocalDateTime.now());
            request.setStatus(status);
            return RequestMapper.toDto(requestRepository.save(request));
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .status(status)
                .event(event)
                .requester(requester)
                .build();
        return RequestMapper.toDto(requestRepository.save(request));
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        eventService.getUserEventOrThrow(userId, eventId);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto) {
        Event event = eventService.getUserEventOrThrow(userId, eventId);
        if (Boolean.FALSE.equals(event.getRequestModeration())) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }
        List<Long> requestIds = dto.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }
        RequestStatus newStatus = EnumUtil.parse(RequestStatus.class, dto.getStatus());
        if (newStatus != RequestStatus.CONFIRMED && newStatus != RequestStatus.REJECTED) {
            throw new BadRequestException("Incorrectly made request.");
        }
        List<ParticipationRequest> requests = loadRequestsForEvent(eventId, requestIds);
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (newStatus == RequestStatus.CONFIRMED) {
                long confirmedCount = confirmedRequestsService.getConfirmedCount(eventId);
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("The participant limit has been reached");
                }
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
            if (newStatus == RequestStatus.CONFIRMED) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(RequestMapper.toDto(requestRepository.save(request)));
                long confirmedAfterSave = confirmedRequestsService.getConfirmedCount(eventId);
                if (event.getParticipantLimit() > 0 && confirmedAfterSave >= event.getParticipantLimit()) {
                    rejectRemainingPending(eventId, rejected);
                    break;
                }
            } else if (newStatus == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(requestRepository.save(request)));
            }
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be canceled");
        }
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(requestRepository.save(request));
    }

    private List<ParticipationRequest> loadRequestsForEvent(Long eventId, List<Long> requestIds) {
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Request was not found");
        }
        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request was not found");
            }
        }
        return requests;
    }

    private RequestStatus resolveInitialRequestStatus(Event event) {
        if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }

    private void rejectRemainingPending(Long eventId, List<ParticipationRequestDto> rejected) {
        List<ParticipationRequest> pending = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
        for (ParticipationRequest request : pending) {
            request.setStatus(RequestStatus.REJECTED);
            rejected.add(RequestMapper.toDto(requestRepository.save(request)));
        }
    }
}
