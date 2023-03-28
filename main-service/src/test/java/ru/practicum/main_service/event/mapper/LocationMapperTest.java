package ru.practicum.main_service.event.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.model.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LocationMapperTest {
    @InjectMocks
    private LocationMapperImpl locationMapper;

    private final LocationDto locationDto = LocationDto.builder()
            .lat(10.7521F)
            .lon(-5.0047F)
            .build();
    private final Location location = Location.builder()
            .id(1L)
            .lat(locationDto.getLat())
            .lon(locationDto.getLon())
            .build();

    @Nested
    class ToLocation {
        @Test
        public void shouldReturnLocation() {
            Location result = locationMapper.toLocation(locationDto);

            assertNull(result.getId());
            assertEquals(locationDto.getLat(), result.getLat());
            assertEquals(locationDto.getLon(), result.getLon());
        }

        @Test
        public void shouldReturnNull() {
            Location result = locationMapper.toLocation(null);

            assertNull(result);
        }
    }

    @Nested
    class ToLocationDto {
        @Test
        public void shouldReturnLocationDto() {
            LocationDto result = locationMapper.toLocationDto(location);

            assertEquals(locationDto.getLat(), result.getLat());
            assertEquals(locationDto.getLon(), result.getLon());
        }

        @Test
        public void shouldReturnNull() {
            LocationDto result = locationMapper.toLocationDto(null);

            assertNull(result);
        }
    }
}
