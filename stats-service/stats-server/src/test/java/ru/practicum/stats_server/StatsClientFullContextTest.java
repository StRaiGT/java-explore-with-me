package ru.practicum.stats_server;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.stats_client.StatsClient;
import ru.practicum.stats_common.model.EndpointHit;
import ru.practicum.stats_common.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {StatsClient.class, StatsServiceApp.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StatsClientFullContextTest {
    private final StatsClient statsClient;
    private final EndpointHit endpointHit = EndpointHit.builder()
            .app("test APP")
            .uri("/test/uri/1")
            .ip("127.0.0.1")
            .timestamp("2020-05-05 10:00:00")
            .build();

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddHitAndGetStats()  {
        ResponseEntity<Object> response = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10));
        try {
            List<ViewStats> stats = (List<ViewStats>) response.getBody();

            assertNotNull(stats);
            assertTrue(stats.isEmpty());
        } catch (ClassCastException exception) {
            throw new ClassCastException("Ошибка приведения типов.");
        }

        statsClient.addHit(endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp(), LocalDateTime.now());
        response = statsClient.getStats(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        try {
            List<ViewStats> stats = (List<ViewStats>) response.getBody();

            assertNotNull(stats);
            assertEquals(1, stats.size());
        } catch (ClassCastException exception) {
            throw new ClassCastException("Ошибка приведения типов.");
        }
    }
}

