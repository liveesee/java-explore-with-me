package ru.practicum.main.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.model.EventSort;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventSearchParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    @Builder.Default
    private boolean onlyAvailable = false;
    private EventSort sort;
    @Builder.Default
    private int from = 0;
    @Builder.Default
    private int size = 10;
}
