package ru.practicum.main_service.comment;

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
import ru.practicum.main_service.comment.model.Comment;
import ru.practicum.main_service.comment.repository.CommentRepository;
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
public class CommentRepositoryTest {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final User user1 = User.builder()
            .id(1L)
            .name("test user 1")
            .email("test1@yandex.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("test user 2")
            .email("test2@yandex.ru")
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
            .annotation("test annotation 1")
            .description("test description 1")
            .title("test title 1")
            .createdOn(LocalDateTime.now().minusDays(7))
            .publishedOn(LocalDateTime.now().minusDays(6))
            .paid(false)
            .state(EventState.PUBLISHED)
            .location(location)
            .category(category)
            .initiator(user1)
            .participantLimit(0)
            .eventDate(LocalDateTime.now().plusHours(3))
            .requestModeration(false)
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .annotation("test annotation 2")
            .description("test description 2")
            .title("test title 2")
            .createdOn(LocalDateTime.now().minusDays(7))
            .publishedOn(LocalDateTime.now().minusDays(6))
            .paid(false)
            .state(EventState.PUBLISHED)
            .location(location)
            .category(category)
            .initiator(user1)
            .participantLimit(0)
            .eventDate(LocalDateTime.now().plusHours(3))
            .requestModeration(false)
            .build();
    private final Event event3 = Event.builder()
            .id(3L)
            .annotation("test annotation 3")
            .description("test description 3")
            .title("test title 3")
            .createdOn(LocalDateTime.now().minusDays(3))
            .publishedOn(LocalDateTime.now().minusDays(2))
            .paid(false)
            .state(EventState.PUBLISHED)
            .location(location)
            .category(category)
            .initiator(user1)
            .participantLimit(0)
            .eventDate(LocalDateTime.now().plusHours(5))
            .requestModeration(false)
            .build();
    private final Comment comment1 = Comment.builder()
            .id(1L)
            .text("test comment 1")
            .author(user1)
            .event(event1)
            .createdOn(LocalDateTime.now().minusHours(8))
            .editedOn(null)
            .build();
    private final Comment comment2 = Comment.builder()
            .id(2L)
            .text("test comment 2")
            .author(user1)
            .event(event1)
            .createdOn(LocalDateTime.now().minusHours(7))
            .editedOn(LocalDateTime.now().minusHours(5))
            .build();
    private final Comment comment3 = Comment.builder()
            .id(3L)
            .text("test comment 3")
            .author(user1)
            .event(event2)
            .createdOn(LocalDateTime.now().minusHours(8))
            .editedOn(null)
            .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        categoryRepository.save(category);
        locationRepository.save(location);
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
    }

    @Nested
    class FindAllByAuthorId {
        @Test
        public void shouldGetThree() {
            List<Comment> commentsFromRepository = commentRepository.findAllByAuthorId(user1.getId());

            assertEquals(3, commentsFromRepository.size());

            Comment commentFromRepository1 = commentsFromRepository.get(0);
            Comment commentFromRepository2 = commentsFromRepository.get(1);
            Comment commentFromRepository3 = commentsFromRepository.get(2);

            checkResult(comment1, commentFromRepository1);
            checkResult(comment2, commentFromRepository2);
            checkResult(comment3, commentFromRepository3);
        }

        @Test
        public void shouldGetEmpty() {
            List<Comment> commentsFromRepository = commentRepository.findAllByAuthorId(user2.getId());

            assertTrue(commentsFromRepository.isEmpty());
        }
    }

    @Nested
    class FindAllByAuthorIdAndEventId {
        @Test
        public void shouldGetTwo() {
            List<Comment> commentsFromRepository = commentRepository.findAllByAuthorIdAndEventId(user1.getId(), event1.getId());

            assertEquals(2, commentsFromRepository.size());

            Comment commentFromRepository1 = commentsFromRepository.get(0);
            Comment commentFromRepository2 = commentsFromRepository.get(1);

            checkResult(comment1, commentFromRepository1);
            checkResult(comment2, commentFromRepository2);
        }

        @Test
        public void shouldGetOne() {
            List<Comment> commentsFromRepository = commentRepository.findAllByAuthorIdAndEventId(user1.getId(), event2.getId());

            assertEquals(1, commentsFromRepository.size());

            Comment commentFromRepository1 = commentsFromRepository.get(0);

            checkResult(comment3, commentFromRepository1);
        }

        @Test
        public void shouldGetEmpty() {
            List<Comment> commentsFromRepository = commentRepository.findAllByAuthorIdAndEventId(user2.getId(), event1.getId());

            assertTrue(commentsFromRepository.isEmpty());
        }
    }

    @Nested
    class FindAllByEventId {
        @Test
        public void shouldGetTwo() {
            List<Comment> commentsFromRepository = commentRepository.findAllByEventId(event1.getId(), pageable);

            assertEquals(2, commentsFromRepository.size());

            Comment commentFromRepository1 = commentsFromRepository.get(0);
            Comment commentFromRepository2 = commentsFromRepository.get(1);

            checkResult(comment1, commentFromRepository1);
            checkResult(comment2, commentFromRepository2);
        }

        @Test
        public void shouldGetOne() {
            List<Comment> commentsFromRepository = commentRepository.findAllByEventId(event2.getId(), pageable);

            assertEquals(1, commentsFromRepository.size());

            Comment commentFromRepository1 = commentsFromRepository.get(0);

            checkResult(comment3, commentFromRepository1);
        }

        @Test
        public void shouldGetEmpty() {
            List<Comment> commentsFromRepository = commentRepository.findAllByEventId(event3.getId(), pageable);

            assertTrue(commentsFromRepository.isEmpty());
        }
    }

    private void checkResult(Comment comment, Comment result) {
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getAuthor(), result.getAuthor());
        assertEquals(comment.getEvent().getId(), result.getEvent().getId());
        assertEquals(comment.getCreatedOn(), result.getCreatedOn());
        assertEquals(comment.getEditedOn(), result.getEditedOn());
    }
}
