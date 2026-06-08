package ru.practicum.main.controller.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateCommentController.class)
class PrivateCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void create_returns201() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .author(UserShortDto.builder().id(1L).name("User").build())
                .event(10L)
                .created(LocalDateTime.now())
                .build();
        when(commentService.create(eq(1L), eq(10L), any())).thenReturn(dto);

        mockMvc.perform(post("/users/1/comments")
                        .param("eventId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(NewCommentDto.builder().text("comment").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns200() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("updated")
                .author(UserShortDto.builder().id(1L).name("User").build())
                .event(10L)
                .created(LocalDateTime.now())
                .build();
        when(commentService.update(eq(1L), eq(1L), any())).thenReturn(dto);

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateCommentDto.builder().text("updated").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("updated"));
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(commentService).deleteByUser(1L, 1L);

        mockMvc.perform(delete("/users/1/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserComments_returnsList() throws Exception {
        when(commentService.getUserComments(1L, null, 0, 10)).thenReturn(List.of(
                CommentDto.builder().id(1L).text("comment").build()));

        mockMvc.perform(get("/users/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
