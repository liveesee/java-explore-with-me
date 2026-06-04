package ru.practicum.main.controller.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.service.RequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateRequestController.class)
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    @Test
    void getUserRequests_returnsList() throws Exception {
        when(requestService.getUserRequests(1L)).thenReturn(List.of(
                ParticipationRequestDto.builder().id(100L).status("PENDING").build()));

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void createRequest_returns201() throws Exception {
        when(requestService.createRequest(1L, 10L))
                .thenReturn(ParticipationRequestDto.builder().id(100L).status("PENDING").build());

        mockMvc.perform(post("/users/1/requests").param("eventId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void cancelRequest_returnsCanceledRequest() throws Exception {
        when(requestService.cancelRequest(1L, 100L))
                .thenReturn(ParticipationRequestDto.builder().id(100L).status("CANCELED").build());

        mockMvc.perform(patch("/users/1/requests/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void getEventRequests_returnsList() throws Exception {
        when(requestService.getEventRequests(1L, 10L)).thenReturn(List.of(
                ParticipationRequestDto.builder().id(100L).status("PENDING").build()));

        mockMvc.perform(get("/users/1/events/10/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void changeRequestStatus_returnsResult() throws Exception {
        EventRequestStatusUpdateRequest dto = EventRequestStatusUpdateRequest.builder()
                .status("CONFIRMED")
                .requestIds(List.of(100L))
                .build();
        when(requestService.changeRequestStatus(eq(1L), eq(10L), any()))
                .thenReturn(EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(List.of(ParticipationRequestDto.builder().id(100L).build()))
                        .rejectedRequests(List.of())
                        .build());

        mockMvc.perform(patch("/users/1/events/10/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(100));
    }
}
