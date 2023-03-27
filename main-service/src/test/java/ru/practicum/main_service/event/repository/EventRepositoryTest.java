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
            .eventDate(LocalDateTime.now().plusDays(6))
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
    private final Event event3 = Event.builder()
            .id(3L)
            .title("test title 3")
            .annotation("test annotation 3")
            .description("test description 3")
            .eventDate(LocalDateTime.now().plusDays(4))
            .category(category)
            .location(location)
            .paid(true)
            .participantLimit(0)
            .requestModeration(true)
            .initiator(user)
            .state(EventState.PUBLISHED)
            .createdOn(LocalDateTime.now().minusDays(2))
            .publishedOn(LocalDateTime.now().minusHours(6))
            .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user);
        categoryRepository.save(category);
        locationRepository.save(location);
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
    }

    @Nested
    class FindAllByInitiatorId {
        @Test
        public void shouldGetTwo() {
            List<Event> eventsFromRepository = eventRepository.findAllByInitiatorId(user.getId(), pageable);

            assertEquals(3, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);
            Event eventFromRepository3 = eventsFromRepository.get(2);

            assertEquals(event1.getId(), eventFromRepository1.getId());
            assertEquals(event2.getId(), eventFromRepository2.getId());
            assertEquals(event3.getId(), eventFromRepository3.getId());
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

    @Nested
    class GetEventsByAdmin {
        @Test
        public void shouldGetTwo() {
            List<Event> eventsFromRepository = eventRepository.getEventsByAdmin(List.of(user.getId()),
                    List.of(EventState.values()), List.of(category.getId()), LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(5),0, 10);

            assertEquals(2, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);

            assertEquals(event1.getId(), eventFromRepository1.getId());
            assertEquals(event3.getId(), eventFromRepository2.getId());
        }

        @Test
        public void shouldGetOne() {
            List<Event> eventsFromRepository = eventRepository.getEventsByAdmin(List.of(user.getId()),
                    List.of(EventState.PUBLISHED), List.of(category.getId()), LocalDateTime.now().plusDays(5),
                    LocalDateTime.now().plusDays(6),0, 10);

            assertEquals(1, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);

            assertEquals(event2.getId(), eventFromRepository1.getId());
        }

        @Test
        public void shouldGetAll() {
            List<Event> eventsFromRepository = eventRepository.getEventsByAdmin(null, null, null,
                    null, null,0, 10);

            assertEquals(3, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);
            Event eventFromRepository3 = eventsFromRepository.get(2);

            assertEquals(event1.getId(), eventFromRepository1.getId());
            assertEquals(event2.getId(), eventFromRepository2.getId());
            assertEquals(event3.getId(), eventFromRepository3.getId());
        }

        @Test
        public void shouldGetEmpty() {
            List<Event> eventsFromRepository = eventRepository.getEventsByAdmin(List.of(2L), null, null,
                    null, null,0, 10);

            assertTrue(eventsFromRepository.isEmpty());
        }

    }

    @Nested
    class GetEventsByPublic {
        @Test
        public void shouldGetTwo() {
            List<Event> eventsFromRepository = eventRepository.getEventsByPublic("TeSt", List.of(category.getId()),
                    true, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(7), 0, 10);

            assertEquals(2, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);

            assertEquals(event2.getId(), eventFromRepository1.getId());
            assertEquals(event3.getId(), eventFromRepository2.getId());
        }

        @Test
        public void shouldGetOne() {
            List<Event> eventsFromRepository = eventRepository.getEventsByPublic("TiON 2", List.of(category.getId()),
                    true, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(7), 0, 10);

            assertEquals(1, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);

            assertEquals(event2.getId(), eventFromRepository1.getId());
        }

        @Test
        public void shouldGetAllPublished() {
            List<Event> eventsFromRepository = eventRepository.getEventsByPublic(null, null, null,
                    null, null, 0, 10);

            assertEquals(2, eventsFromRepository.size());

            Event eventFromRepository1 = eventsFromRepository.get(0);
            Event eventFromRepository2 = eventsFromRepository.get(1);

            assertEquals(event2.getId(), eventFromRepository1.getId());
            assertEquals(event3.getId(), eventFromRepository2.getId());
        }

        @Test
        public void shouldGetEmpty() {
            List<Event> eventsFromRepository = eventRepository.getEventsByPublic("not exist text", List.of(category.getId()),
                    true, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(7), 0, 10);

            assertTrue(eventsFromRepository.isEmpty());
        }
    }

}
