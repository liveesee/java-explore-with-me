package ru.practicum.main.controller.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.NewEventDto;
import ru.practicum.main.dto.UpdateEventUserRequest;
import ru.practicum.main.service.EventService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateEventController.class)
class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    void createEvent_returns201() throws Exception {
        NewEventDto dto = NewEventDto.builder()
                .annotation("annotation with enough length for validation")
                .description("description with enough length for validation rules")
                .title("Title")
                .category(1L)
                .eventDate("2026-12-01 12:00:00")
                .location(ru.practicum.main.dto.LocationDto.builder().lat(55.75f).lon(37.62f).build())
                .build();
        when(eventService.create(eq(1L), any())).thenReturn(EventFullDto.builder().id(10L).title("Title").build());

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getEvents_returnsList() throws Exception {
        when(eventService.getUserEvents(1L, 0, 10)).thenReturn(
                List.of(EventShortDto.builder().id(10L).title("Title").build()));

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getEvent_returnsEvent() throws Exception {
        when(eventService.getUserEvent(1L, 10L)).thenReturn(EventFullDto.builder().id(10L).title("Title").build());

        mockMvc.perform(get("/users/1/events/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void updateEvent_returnsUpdatedEvent() throws Exception {
        UpdateEventUserRequest dto = UpdateEventUserRequest.builder().title("Updated").build();
        when(eventService.updateUserEvent(eq(1L), eq(10L), any()))
                .thenReturn(EventFullDto.builder().id(10L).title("Updated").build());

        mockMvc.perform(patch("/users/1/events/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }
}
