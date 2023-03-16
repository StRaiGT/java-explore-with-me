package ru.practicum.stats_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.stats_common.model.EndpointHit;
import ru.practicum.stats_service.model.Stats;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    @Mapping(target = "timestamp", expression = "java(timestamp)")
    Stats toStats(EndpointHit endpointHit, LocalDateTime timestamp);
}
