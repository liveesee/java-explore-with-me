package ru.practicum.main.controller.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.param.PublicEventSearchParams;
import ru.practicum.main.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEvents(@ModelAttribute PublicEventSearchParams params,
                                         HttpServletRequest request) {
        List<EventShortDto> events = eventService.getPublicEvents(params);
        statsClient.hit("/events", request);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        statsClient.hit("/events/" + id, request);
        return eventService.getPublicEvent(id);
    }
}
