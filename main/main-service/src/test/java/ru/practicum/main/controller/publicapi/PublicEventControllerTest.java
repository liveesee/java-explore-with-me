package ru.practicum.main.controller.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.service.EventService;
import ru.practicum.main.stats.StatsService;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;
    @MockBean
    private StatsService statsService;

    @Test
    void getEvents_returnsListAndRecordsHit() throws Exception {
        when(eventService.getPublicEvents(null, null, null, null, null, false, null, 0, 10))
                .thenReturn(List.of(EventShortDto.builder().id(1L).title("Event").build()));
        doNothing().when(statsService).hit("/events");

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Event"));
    }

    @Test
    void getEvent_returnsEventAndRecordsHit() throws Exception {
        when(eventService.getPublicEvent(1L)).thenReturn(EventFullDto.builder().id(1L).title("Event").build());
        doNothing().when(statsService).hit("/events/1");

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
