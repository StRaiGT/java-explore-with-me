package ru.practicum.main_service.compilation;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.repository.CategoryRepository;
import ru.practicum.main_service.compilation.model.Compilation;
import ru.practicum.main_service.compilation.repository.CompilationRepository;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.event.repository.EventRepository;
import ru.practicum.main_service.event.repository.LocationRepository;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompilationRepositoryTest {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    private final User user = User.builder()
            .id(1L)
            .name("test user")
            .email("test@email.ru")
            .build();
    private final Category category = Category.builder()
            .id(1L)
            .name("test category")
            .build();
    private final Location location = Location.builder()
            .id(1L)
            .lat(10F)
            .lon(10F)
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .annotation("test annotation")
            .description("test description")
            .title("test title")
            .createdOn(LocalDateTime.now())
            .paid(false)
            .state(EventState.PENDING)
            .location(location)
            .category(category)
            .initiator(user)
            .participantLimit(0)
            .eventDate(LocalDateTime.now().plusHours(3))
            .requestModeration(false)
            .build();
    private final Compilation compilation1 = Compilation.builder()
            .id(1L)
            .title("test title 1")
            .pinned(true)
            .events(List.of(event1))
            .build();
    private final Compilation compilation2 = Compilation.builder()
            .id(2L)
            .title("test title 2")
            .pinned(true)
            .events(List.of())
            .build();
    private final Compilation compilation3 = Compilation.builder()
            .id(3L)
            .title("test title 3")
            .pinned(false)
            .events(List.of(event1))
            .build();
    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user);
        categoryRepository.save(category);
        locationRepository.save(location);
        eventRepository.save(event1);
        compilationRepository.save(compilation1);
        compilationRepository.save(compilation2);
    }

    @Nested
    class FindAllByPinned {
        @Test
        public void shouldGetTwoIfPinnedIsTrue() {
            compilationRepository.save(compilation3);

            List<Compilation> compilationsFromRepository = compilationRepository.findAllByPinned(true, pageable);

            assertEquals(2, compilationsFromRepository.size());

            Compilation compilationFromRepository1 = compilationsFromRepository.get(0);
            Compilation compilationFromRepository2 = compilationsFromRepository.get(1);

            checkResult(compilation1, compilationFromRepository1);
            checkResult(compilation2, compilationFromRepository2);
        }

        @Test
        public void shouldGetOneIfPinnedIsFalse() {
            compilationRepository.save(compilation3);

            List<Compilation> compilationsFromRepository = compilationRepository.findAllByPinned(false, pageable);

            assertEquals(1, compilationsFromRepository.size());

            Compilation compilationFromRepository1 = compilationsFromRepository.get(0);

            checkResult(compilation3, compilationFromRepository1);
        }

        @Test
        public void shouldGetEmptyIfNoPinnedIsFalse() {
            List<Compilation> compilationsFromRepository = compilationRepository.findAllByPinned(false, pageable);

            assertTrue(compilationsFromRepository.isEmpty());
        }
    }

    private void checkResult(Compilation compilation, Compilation compilationFromRepository) {
        assertEquals(compilation.getId(), compilationFromRepository.getId());
        assertEquals(compilation.getTitle(), compilationFromRepository.getTitle());
        assertEquals(compilation.getPinned(), compilationFromRepository.getPinned());
        assertEquals(compilation.getEvents().size(), compilationFromRepository.getEvents().size());
    }
}
