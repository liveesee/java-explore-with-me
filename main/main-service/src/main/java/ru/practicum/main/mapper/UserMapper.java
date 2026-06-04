package ru.practicum.main.mapper;

import ru.practicum.main.dto.UserDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
