package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmedRequestsServiceTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @InjectMocks
    private ConfirmedRequestsService confirmedRequestsService;

    @Test
    void getConfirmedCounts_emptyIds_returnsEmptyMap() {
        assertTrue(confirmedRequestsService.getConfirmedCounts(Collections.emptyList()).isEmpty());
        assertTrue(confirmedRequestsService.getConfirmedCounts(null).isEmpty());
    }

    @Test
    void getConfirmedCounts_returnsMap() {
        ParticipationRequestRepository.EventConfirmedCount count =
                mock(ParticipationRequestRepository.EventConfirmedCount.class);
        when(count.getEventId()).thenReturn(1L);
        when(count.getCount()).thenReturn(3L);
        when(requestRepository.countConfirmedByEventIds(List.of(1L), RequestStatus.CONFIRMED))
                .thenReturn(List.of(count));

        assertEquals(3L, confirmedRequestsService.getConfirmedCounts(List.of(1L)).get(1L));
    }

    @Test
    void getConfirmedCount_delegatesToRepository() {
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        assertEquals(5L, confirmedRequestsService.getConfirmedCount(1L));
    }
}
