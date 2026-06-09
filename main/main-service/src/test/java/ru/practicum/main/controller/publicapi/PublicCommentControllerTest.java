package ru.practicum.main.controller.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCommentController.class)
class PublicCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void getByEvent_returnsList() throws Exception {
        when(commentService.getByEventId(10L, 0, 10)).thenReturn(List.of(
                CommentDto.builder().id(1L).text("comment").build()));

        mockMvc.perform(get("/comments").param("eventId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_returnsComment() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .author(UserShortDto.builder().id(1L).name("User").build())
                .event(10L)
                .created(LocalDateTime.now())
                .build();
        when(commentService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
