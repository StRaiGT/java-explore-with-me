package ru.practicum.main_service.event.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.main_service.event.model.Location;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LocationRepositoryTest {
    private final LocationRepository locationRepository;

    private final Location location1 = Location.builder()
            .id(1L)
            .lon(-12.345F)
            .lat(5.4321F)
            .build();
    private final Location location2 = Location.builder()
            .id(2L)
            .lon(5.4321F)
            .lat(-4.4566F)
            .build();

    @BeforeEach
    public void beforeEach() {
        locationRepository.save(location1);
        locationRepository.save(location2);
    }

    @Nested
    class FindByLatAndLon {
        @Test
        public void shouldGet() {
            Optional<Location> optionalLocation = locationRepository.findByLatAndLon(location2.getLat(),
                    location2.getLon());

            assertTrue(optionalLocation.isPresent());

            Location locationFromRepository = optionalLocation.get();

            assertEquals(location2, locationFromRepository);
        }

        @Test
        public void shouldGetEmpty() {
            Optional<Location> optionalLocation = locationRepository.findByLatAndLon(0F, 0F);

            assertTrue(optionalLocation.isEmpty());
        }
    }
}
