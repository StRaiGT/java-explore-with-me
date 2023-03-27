package ru.practicum.main_service.compilation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.compilation.dto.CompilationDto;
import ru.practicum.main_service.compilation.dto.NewCompilationDto;
import ru.practicum.main_service.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main_service.compilation.mapper.CompilationMapperImpl;
import ru.practicum.main_service.compilation.model.Compilation;
import ru.practicum.main_service.compilation.repository.CompilationRepository;
import ru.practicum.main_service.compilation.service.CompilationServiceImpl;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.service.EventService;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompilationServiceTest {
    @Mock
    private EventService eventService;

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private CompilationMapperImpl compilationMapper;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    @Captor
    private ArgumentCaptor<Compilation> compilationArgumentCaptor;

    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final User user1 = User.builder()
            .id(1L)
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .build();
    private final Category category1 = Category.builder()
            .id(1L)
            .name("test category 1")
            .build();
    private final Category category2 = Category.builder()
            .id(2L)
            .name("test category 2")
            .build();
    private final NewCompilationDto newCompilationDto1 = NewCompilationDto.builder()
            .title("test title 1")
            .pinned(false)
            .events(List.of(1L, 2L))
            .build();
    private final NewCompilationDto newCompilationDto2 = NewCompilationDto.builder()
            .title("test title 2")
            .pinned(true)
            .events(List.of())
            .build();
    private final UpdateCompilationRequest updateCompilationRequest1 = UpdateCompilationRequest.builder()
            .title("test title update")
            .pinned(true)
            .events(List.of(1L))
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .category(category1)
            .initiator(user1)
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .category(category2)
            .initiator(user2)
            .build();
    private final EventShortDto eventShortDto1 = EventShortDto.builder()
            .id(event1.getId())
            .views(0L)
            .confirmedRequests(10L)
            .build();
    private final EventShortDto eventShortDto2 = EventShortDto.builder()
            .id(event2.getId())
            .views(10L)
            .confirmedRequests(0L)
            .build();
    private final Compilation compilation1 = Compilation.builder()
            .id(1L)
            .title(newCompilationDto1.getTitle())
            .pinned(newCompilationDto1.getPinned())
            .events(List.of(event1, event2))
            .build();
    private final Compilation compilation2 = Compilation.builder()
            .id(2L)
            .title(newCompilationDto2.getTitle())
            .pinned(newCompilationDto2.getPinned())
            .events(List.of())
            .build();
    private final Compilation updatedCompilation1 = Compilation.builder()
            .id(compilation1.getId())
            .title(updateCompilationRequest1.getTitle())
            .pinned(updateCompilationRequest1.getPinned())
            .events(List.of(event1))
            .build();
    private final CompilationDto compilationDto1 = CompilationDto.builder()
            .id(compilation1.getId())
            .title(compilation1.getTitle())
            .pinned(compilation1.getPinned())
            .events(List.of(eventShortDto1, eventShortDto2))
            .build();
    private final CompilationDto compilationDto2 = CompilationDto.builder()
            .id(compilation2.getId())
            .title(compilation2.getTitle())
            .pinned(compilation2.getPinned())
            .events(List.of())
            .build();
    private final CompilationDto updatedCompilationDto1 = CompilationDto.builder()
            .id(updatedCompilation1.getId())
            .title(updatedCompilation1.getTitle())
            .pinned(updatedCompilation1.getPinned())
            .events(List.of(eventShortDto1))
            .build();

    @Nested
    class Create {
        @Test
        public void shouldCreate() {
            when(eventService.getEventsByIds(any())).thenReturn(List.of(event1, event2));
            when(compilationMapper.newDtoToCompilation(any(), any())).thenCallRealMethod();
            when(compilationRepository.save(any())).thenReturn(compilation1);
            when(compilationRepository.findById(any())).thenReturn(Optional.of(compilation1));
            when(eventService.toEventsShortDto(List.of(event1, event2))).thenReturn(List.of(eventShortDto1, eventShortDto2));
            when(compilationMapper.toCompilationDto(any(), any())).thenCallRealMethod();

            CompilationDto savedCompilationDto = compilationService.create(newCompilationDto1);

            checkResults(compilationDto1, savedCompilationDto);

            verify(eventService, times(1)).getEventsByIds(any());
            verify(compilationMapper, times(1)).newDtoToCompilation(any(), any());
            verify(compilationRepository, times(1)).save(compilationArgumentCaptor.capture());
            verify(compilationRepository, times(1)).findById(any());
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(1)).toCompilationDto(any(), any());

            Compilation savedCompilation = compilationArgumentCaptor.getValue();

            assertNull(savedCompilation.getId());
            checkResults(compilation1, savedCompilation);
        }

        @Test
        public void shouldCreateWithEmptyEvents() {
            when(compilationMapper.newDtoToCompilation(any(), any())).thenCallRealMethod();
            when(compilationRepository.save(any())).thenReturn(compilation2);
            when(compilationRepository.findById(any())).thenReturn(Optional.of(compilation2));
            when(eventService.toEventsShortDto(List.of())).thenReturn(List.of());
            when(compilationMapper.toCompilationDto(any(), any())).thenCallRealMethod();

            CompilationDto savedCompilationDto = compilationService.create(newCompilationDto2);

            checkResults(compilationDto2, savedCompilationDto);

            verify(compilationMapper, times(1)).newDtoToCompilation(any(), any());
            verify(compilationRepository, times(1)).save(compilationArgumentCaptor.capture());
            verify(compilationRepository, times(1)).findById(any());
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(1)).toCompilationDto(any(), any());

            Compilation savedCompilation = compilationArgumentCaptor.getValue();

            assertNull(savedCompilation.getId());
            checkResults(compilation2, savedCompilation);
        }

        @Test
        public void shouldThrowExceptionIfEventNotFound() {
            when(eventService.getEventsByIds(any())).thenReturn(List.of(event1));

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> compilationService.create(newCompilationDto1));
            assertEquals("Некоторые события не найдены.", exception.getMessage());

            verify(eventService, times(1)).getEventsByIds(any());
            verify(compilationRepository, never()).save(any());
        }
    }

    @Nested
    class Patch {
        @Test
        public void shouldPatch() {
            when(compilationRepository.findById(any())).thenReturn(Optional.of(compilation1));
            when(eventService.getEventsByIds(any())).thenReturn(List.of(event1));
            when(compilationRepository.save(any())).thenReturn(updatedCompilation1);
            when(eventService.toEventsShortDto(List.of(event1))).thenReturn(List.of(eventShortDto1));
            when(compilationMapper.toCompilationDto(any(), any())).thenCallRealMethod();

            CompilationDto savedCompilationDto = compilationService.patch(compilation1.getId(), updateCompilationRequest1);

            checkResults(updatedCompilationDto1, savedCompilationDto);

            verify(compilationRepository, times(2)).findById(any());
            verify(eventService, times(1)).getEventsByIds(any());
            verify(compilationRepository, times(1)).save(compilationArgumentCaptor.capture());
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(1)).toCompilationDto(any(), any());

            Compilation savedCompilation = compilationArgumentCaptor.getValue();

            assertEquals(updatedCompilation1.getId(), savedCompilation.getId());
            checkResults(updatedCompilation1, savedCompilation);
        }

        @Test
        public void shouldThrowExceptionIfCompilationNotFound() {
            when(compilationRepository.findById(any())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> compilationService.patch(compilation1.getId(), updateCompilationRequest1));
            assertEquals("Подборки с таким id не существует.", exception.getMessage());

            verify(compilationRepository, times(1)).findById(any());
            verify(compilationRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfSomeEventsIdNotFound() {
            when(compilationRepository.findById(any())).thenReturn(Optional.of(compilation1));
            when(eventService.getEventsByIds(any())).thenReturn(List.of());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> compilationService.patch(compilation1.getId(), updateCompilationRequest1));
            assertEquals("Некоторые события не найдены.", exception.getMessage());

            verify(compilationRepository, times(1)).findById(any());
            verify(compilationRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteById {
        @Test
        public void shouldDelete() {
            when(compilationRepository.findById(any())).thenReturn(Optional.of(compilation1));

            compilationService.deleteById(compilation1.getId());

            verify(compilationRepository, times(1)).findById(any());
            verify(compilationRepository, times(1)).deleteById(any());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(compilationRepository.findById(any())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> compilationService.deleteById(compilation1.getId()));
            assertEquals("Подборки с таким id не существует.", exception.getMessage());

            verify(compilationRepository, times(1)).findById(any());
            verify(compilationRepository, never()).deleteById(any());
        }
    }

    @Nested
    class GetAll {
        @Test
        public void shouldGetIfPinnedIsNull() {
            when(compilationRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(compilation1, compilation2)));
            when(eventService.toEventsShortDto(any())).thenReturn(List.of(eventShortDto1, eventShortDto2));
            when(compilationMapper.toCompilationDto(ArgumentMatchers.eq(compilation1), ArgumentMatchers.any()))
                    .thenCallRealMethod();
            when(compilationMapper.toCompilationDto(ArgumentMatchers.eq(compilation2), ArgumentMatchers.any()))
                    .thenCallRealMethod();

            List<CompilationDto> savedCompilationsDto = compilationService.getAll(null, pageable);

            verify(compilationRepository, times(1)).findAll(pageable);
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(2)).toCompilationDto(any(), any());

            assertEquals(2, savedCompilationsDto.size());

            CompilationDto savedCompilationDto1 = savedCompilationsDto.get(0);
            CompilationDto savedCompilationDto2 = savedCompilationsDto.get(1);

            checkResults(compilationDto1, savedCompilationDto1);
            checkResults(compilationDto2, savedCompilationDto2);
        }

        @Test
        public void shouldGetIfPinnedIsNotNull() {
            when(compilationRepository.findAllByPinned(compilation2.getPinned(), pageable)).thenReturn(List.of(compilation2));
            when(eventService.toEventsShortDto(any())).thenReturn(List.of());
            when(compilationMapper.toCompilationDto(ArgumentMatchers.eq(compilation2), ArgumentMatchers.any()))
                    .thenCallRealMethod();

            List<CompilationDto> savedCompilationsDto = compilationService.getAll(compilation2.getPinned(), pageable);

            verify(compilationRepository, times(1)).findAllByPinned(any(), any());
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(1)).toCompilationDto(any(), any());

            assertEquals(1, savedCompilationsDto.size());

            CompilationDto savedCompilationDto1 = savedCompilationsDto.get(0);

            checkResults(compilationDto2, savedCompilationDto1);
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGet() {
            when(compilationRepository.findById(compilation1.getId())).thenReturn(Optional.of(compilation1));
            when(eventService.toEventsShortDto(compilation1.getEvents())).thenReturn(List.of(eventShortDto1, eventShortDto2));
            when(compilationMapper.toCompilationDto(ArgumentMatchers.eq(compilation1), ArgumentMatchers.any()))
                    .thenCallRealMethod();

            CompilationDto savedCompilationsDto = compilationService.getById(compilation1.getId());

            checkResults(compilationDto1, savedCompilationsDto);

            verify(compilationRepository, times(1)).findById(compilation1.getId());
            verify(eventService, times(1)).toEventsShortDto(any());
            verify(compilationMapper, times(1)).toCompilationDto(any(), any());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(compilationRepository.findById(compilation1.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> compilationService.getById(compilation1.getId()));
            assertEquals("Подборки с таким id не существует.", exception.getMessage());

            verify(compilationRepository, times(1)).findById(compilation1.getId());
        }
    }

    private void checkResults(CompilationDto compilationDto, CompilationDto result) {
        assertEquals(compilationDto.getId(), result.getId());
        assertEquals(compilationDto.getTitle(), result.getTitle());
        assertEquals(compilationDto.getPinned(), result.getPinned());
        assertEquals(compilationDto.getEvents().size(), result.getEvents().size());
    }

    private void checkResults(Compilation compilation, Compilation result) {
        assertEquals(compilation.getTitle(), result.getTitle());
        assertEquals(compilation.getPinned(), result.getPinned());
        assertEquals(compilation.getEvents().size(), result.getEvents().size());
    }
}
