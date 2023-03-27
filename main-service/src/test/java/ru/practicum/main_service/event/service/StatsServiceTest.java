package ru.practicum.main_service.event.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.event.dto.RequestStats;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.repository.RequestRepository;
import ru.practicum.stats_client.StatsClient;
import ru.practicum.stats_common.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {
    @Mock
    private StatsClient statsClient;

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private final LocalDateTime startDate = LocalDateTime.parse("2020-08-24 13:30:00", MainCommonUtils.DT_FORMATTER);
    private final LocalDateTime endDate = LocalDateTime.parse("2020-08-24 13:30:00", MainCommonUtils.DT_FORMATTER);
    private final Boolean unique = false;
    private final ViewStats viewStats1 = ViewStats.builder()
            .app("test app")
            .uri("/event/1")
            .hits(7L)
            .build();
    private final ViewStats viewStats2 = ViewStats.builder()
            .app("test app")
            .uri("/event/2")
            .hits(18L)
            .build();
    private final Event event1 = Event.builder()
            .id(1L)
            .createdOn(LocalDateTime.now().minusDays(5))
            .publishedOn(LocalDateTime.now().minusHours(3))
            .build();
    private final Event event2 = Event.builder()
            .id(2L)
            .createdOn(LocalDateTime.now().minusDays(1))
            .publishedOn(LocalDateTime.now().minusDays(3))
            .build();
    private final Event event3 = Event.builder()
            .id(3L)
            .createdOn(LocalDateTime.now().minusDays(1))
            .publishedOn(null)
            .build();
    private final RequestStats requestStats1 = RequestStats.builder()
            .eventId(1L)
            .confirmedRequests(40L)
            .build();
    private final RequestStats requestStats2 = RequestStats.builder()
            .eventId(2L)
            .confirmedRequests(16L)
            .build();

    @Nested
    class AddHit {
        @Test
        public void shouldAdd() {
            statsService.addHit(new MockHttpServletRequest());

            verify(statsClient, times(1)).addHit(any(), any(), any(), any());
        }
    }

    @Nested
    class GetStats {
        @Test
        public void shouldGet() {
            when(statsClient.getStats(startDate, endDate, List.of("/event/1", "/event/2"), unique))
                    .thenReturn(new ResponseEntity<>(List.of(viewStats1, viewStats2), HttpStatus.OK));

            List<ViewStats> viewStatsResponse = statsService.getStats(startDate, endDate, List.of("/event/1", "/event/2"), unique);

            assertEquals(2, viewStatsResponse.size());

            ViewStats viewStatsResponse1 = viewStatsResponse.get(0);
            ViewStats viewStatsResponse2 = viewStatsResponse.get(1);

            assertEquals(viewStats1, viewStatsResponse1);
            assertEquals(viewStats2, viewStatsResponse2);

            verify(statsClient, times(1)).getStats(any(), any(), any(), any());
        }
    }

    @Nested
    class GetViews {
        @Test
        public void shouldGet() {
            when(statsClient.getStats(ArgumentMatchers.eq(event2.getPublishedOn()), ArgumentMatchers.any(),
                    ArgumentMatchers.eq(List.of("/events/1", "/events/2")), ArgumentMatchers.eq(null)))
                    .thenReturn(new ResponseEntity<>(List.of(viewStats1, viewStats2), HttpStatus.OK));

            Map<Long, Long> views = statsService.getViews(List.of(event1, event2, event3));

            assertEquals(2, views.values().size());
            assertEquals(viewStats1.getHits(), views.get(event1.getId()));
            assertEquals(viewStats2.getHits(), views.get(event2.getId()));

            verify(statsClient, times(1)).getStats(any(), any(), any(), any());
        }

        @Test
        public void shouldGetEmpty() {
            Map<Long, Long> views = statsService.getViews(List.of(event3));

            assertTrue(views.values().isEmpty());

            verify(statsClient, never()).getStats(any(), any(), any(), any());
        }
    }

    @Nested
    class GetConfirmedRequests {
        @Test
        public void shouldGet() {
            when(requestRepository.getConfirmedRequests(List.of(event1.getId(), event2.getId())))
                    .thenReturn(List.of(requestStats1, requestStats2));

            Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(List.of(event1, event2, event3));

            assertEquals(2, confirmedRequests.values().size());
            assertEquals(requestStats1.getConfirmedRequests(), confirmedRequests.get(event1.getId()));
            assertEquals(requestStats2.getConfirmedRequests(), confirmedRequests.get(event2.getId()));

            verify(requestRepository, times(1)).getConfirmedRequests(any());
        }

        @Test
        public void shouldGetEmpty() {
            Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(List.of(event3));

            assertTrue(confirmedRequests.values().isEmpty());

            verify(statsClient, never()).getStats(any(), any(), any(), any());
        }
    }
}
