package ru.practicum.main.controller.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.param.PublicEventSearchParams;
import ru.practicum.main.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private StatsClient statsClient;

    @Test
    void getEvents_returnsListAndRecordsHit() throws Exception {
        when(eventService.getPublicEvents(any(PublicEventSearchParams.class)))
                .thenReturn(List.of(EventShortDto.builder().id(1L).title("Event").build()));
        doNothing().when(statsClient).hit(eq("/events"), any(HttpServletRequest.class));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Event"));
    }

    @Test
    void getEvent_returnsEventAndRecordsHit() throws Exception {
        when(eventService.getPublicEvent(1L)).thenReturn(EventFullDto.builder().id(1L).title("Event").build());
        doNothing().when(statsClient).hit(eq("/events/1"), any(HttpServletRequest.class));

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
