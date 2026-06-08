package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfirmedRequestsService {
    private final ParticipationRequestRepository requestRepository;

    public Map<Long, Long> getConfirmedCounts(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return requestRepository.countConfirmedByEventIds(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        ParticipationRequestRepository.EventConfirmedCount::getEventId,
                        ParticipationRequestRepository.EventConfirmedCount::getCount
                ));
    }

    public long getConfirmedCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}
