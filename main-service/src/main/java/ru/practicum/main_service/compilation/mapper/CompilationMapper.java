package ru.practicum.main_service.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main_service.compilation.dto.CompilationDto;
import ru.practicum.main_service.compilation.dto.NewCompilationDto;
import ru.practicum.main_service.compilation.model.Compilation;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "events", expression = "java(events)")
    Compilation newDtoToCompilation(NewCompilationDto newCompilationDto, List<Event> events);

    @Mapping(target = "events", expression = "java(eventsShortDto)")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsShortDto);
}
