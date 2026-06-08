package ru.practicum.main.mapper;

import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.model.ParticipationRequest;

public final class RequestMapper {
    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}
