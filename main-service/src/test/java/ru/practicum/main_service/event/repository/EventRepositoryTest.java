package ru.practicum.main_service.event.repository;

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
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventRepositoryTest {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final User user = User.builder()
            .id(1L)
            .name("test user")
            .email("test@yandex.ru")
            .build();
    private final Category category = Category.builder()
            .id(1L)
            .name("test category")
            .build();
    private final Location location = Location.builder()
            .id(1L)
            .lat(11.1524F)
            .lon(-5.0010F)
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .title("test title 1")
            .annotation("test annotation 1")
            .description("test description 1")
            .eventDate(LocalDateTime.now().plusDays(2))
            .category(category)
            .location(location)
            .paid(false)
            .participantLimit(0)
            .requestModeration(false)
            .initiator(user)
            .state(EventState.PENDING)
            .createdOn(LocalDateTime.now())
            .publishedOn(null)
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .title("test title 2")
            .annotation("test annotation 2")
            .description("test description 2")
            .eventDate(LocalDateTime.now().plusDays(4))
            .category(category)
            .location(location)
            .paid(true)
            .participantLimit(50)
            .requestModeration(true)
            .initiator(user)
            .state(EventState.PUBLISHED)
            .createdOn(LocalDateTime.now().minusDays(1))
            .publishedOn(LocalDateTime.now().minusHours(3))
            .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user);
        categoryRepository.save(category);
        locationRepository.save(location);
        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @Nested
    class FindAllByInitiatorId {
        @Test
        public void shouldGetTwo() {
            List<Event> eventsFromRepository = eventRepository.findAllByInitiatorId(user.getId(), pageable);

            assertEquals(2, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);

            assertEquals(event1.getId(), eventFromRepository1.getId());
            assertEquals(event2.getId(), eventFromRepository2.getId());
        }

        @Test
        public void shouldGetEmpty() {
            List<Event> eventsFromRepository = eventRepository.findAllByInitiatorId(99L, pageable);

            assertTrue(eventsFromRepository.isEmpty());
        }
    }

    @Nested
    class FindByIdAndInitiatorId {
        @Test
        public void shouldGetOne() {
            Optional<Event> optionalEvent = eventRepository.findByIdAndInitiatorId(event2.getId(), user.getId());

            assertTrue(optionalEvent.isPresent());

            Event eventFromRepository = optionalEvent.get();

            assertEquals(event2.getId(), eventFromRepository.getId());

        }

        @Test
        public void shouldGetEmpty() {
            Optional<Event> optionalEvent = eventRepository.findByIdAndInitiatorId(99L, user.getId());

            assertTrue(optionalEvent.isEmpty());
        }
    }

    @Nested
    class FindAllByIdIn {
        @Test
        public void shouldGetTwo() {
            List<Event> eventsFromRepository = eventRepository.findAllByIdIn(List.of(event1.getId(), event2.getId()));

            assertEquals(2, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);

            assertEquals(event1.getId(), eventFromRepository1.getId());
            assertEquals(event2.getId(), eventFromRepository2.getId());
        }

        @Test
        public void shouldGetEmptyIfNotFound() {
            List<Event> eventsFromRepository = eventRepository.findAllByIdIn(List.of(99L));

            assertTrue(eventsFromRepository.isEmpty());
        }

        @Test
        public void shouldGetEmptyIfIdsIsEmpty() {
            List<Event> eventsFromRepository = eventRepository.findAllByIdIn(List.of());

            assertTrue(eventsFromRepository.isEmpty());
        }
    }
}
