package ru.practicum.main_service.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.enums.EventSortType;
import ru.practicum.main_service.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = MainCommonUtils.DT_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = MainCommonUtils.DT_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSortType sort,
            @RequestParam(required = false, defaultValue = MainCommonUtils.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = MainCommonUtils.PAGE_DEFAULT_SIZE) @Positive Integer size,
            HttpServletRequest request) {
        return eventService.getEventsByPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByPublic(@PathVariable Long id,
                                         HttpServletRequest request) {
        return eventService.getEventByPublic(id, request);
    }
}
