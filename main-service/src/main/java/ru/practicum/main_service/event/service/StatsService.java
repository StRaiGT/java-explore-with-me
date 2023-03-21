package ru.practicum.main_service.event.service;

import ru.practicum.stats_common.model.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void addHit(HttpServletRequest request);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
