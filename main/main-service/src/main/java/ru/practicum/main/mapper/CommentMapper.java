package ru.practicum.main.mapper;

import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .event(comment.getEvent().getId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }

    public static Comment toEntity(String text, User author, Event event) {
        return Comment.builder()
                .text(text)
                .created(LocalDateTime.now())
                .author(author)
                .event(event)
                .build();
    }

    public static void applyUpdate(Comment comment, String text) {
        comment.setText(text);
        comment.setUpdated(LocalDateTime.now());
    }
}
