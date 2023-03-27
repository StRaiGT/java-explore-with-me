package ru.practicum.main_service.compilation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.compilation.dto.CompilationDto;
import ru.practicum.main_service.compilation.dto.NewCompilationDto;
import ru.practicum.main_service.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main_service.compilation.mapper.CompilationMapperImpl;
import ru.practicum.main_service.compilation.model.Compilation;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.model.Event;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class CompilationMapperTest {
    @InjectMocks
    private CompilationMapperImpl compilationMapper;

    private final Event event1 = Event.builder()
            .id(1L)
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .build();
    private final EventShortDto eventShortDto1 = EventShortDto.builder()
            .id(1L)
            .build();
    private final EventShortDto eventShortDto2 = EventShortDto.builder()
            .id(2L)
            .build();
    private final NewCompilationDto newCompilationDto = NewCompilationDto.builder()
            .title("test title")
            .pinned(false)
            .events(List.of(event1.getId(), event2.getId()))
            .build();
    private final UpdateCompilationRequest updateCompilationRequest = UpdateCompilationRequest.builder()
            .title(newCompilationDto.getTitle())
            .pinned(newCompilationDto.getPinned())
            .events(List.of(event1.getId(), event2.getId()))
            .build();
    private final Compilation compilation = Compilation.builder()
            .id(1L)
            .title(newCompilationDto.getTitle())
            .pinned(newCompilationDto.getPinned())
            .events(List.of(event1, event2))
            .build();
    private final CompilationDto compilationDto = CompilationDto.builder()
            .id(1L)
            .title(compilation.getTitle())
            .pinned(compilation.getPinned())
            .events(List.of(eventShortDto1, eventShortDto2))
            .build();

    @Nested
    class NewDtoToCompilation {
        @Test
        public void shouldReturnCompilation() {
            Compilation result = compilationMapper.newDtoToCompilation(newCompilationDto, List.of(event1, event2));

            assertNull(result.getId());
            assertEquals(compilation.getTitle(), result.getTitle());
            assertEquals(compilation.getPinned(), result.getPinned());
            assertEquals(compilation.getEvents(), result.getEvents());
        }

        @Test
        public void shouldReturnNull() {
            Compilation result = compilationMapper.newDtoToCompilation(null, null);

            assertNull(result);
        }
    }

    @Nested
    class ToCompilationDto {
        @Test
        public void shouldReturnCompilationDto() {
            CompilationDto result = compilationMapper.toCompilationDto(compilation, List.of(eventShortDto1, eventShortDto2));

            assertEquals(compilationDto.getId(), result.getId());
            assertEquals(compilationDto.getTitle(), result.getTitle());
            assertEquals(compilationDto.getPinned(), result.getPinned());
            assertEquals(compilationDto.getEvents(), result.getEvents());
        }

        @Test
        public void shouldReturnNull() {
            CompilationDto result = compilationMapper.toCompilationDto(null, null);

            assertNull(result);
        }
    }
}
