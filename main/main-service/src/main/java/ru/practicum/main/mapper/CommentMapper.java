package ru.practicum.main.mapper;

import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.model.Comment;

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
}
