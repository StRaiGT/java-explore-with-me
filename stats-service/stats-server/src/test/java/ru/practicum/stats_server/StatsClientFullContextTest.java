package ru.practicum.stats_server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.stats_client.StatsClient;
import ru.practicum.stats_common.StatsCommonUtils;
import ru.practicum.stats_common.model.EndpointHit;
import ru.practicum.stats_common.model.ViewStats;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {StatsClient.class, StatsServiceApp.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StatsClientFullContextTest {
    private final StatsClient statsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final EndpointHit endpointHit1 = EndpointHit.builder()
            .app("test APP")
            .uri("/test/uri/1")
            .ip("127.0.0.1")
            .timestamp("2020-05-05 10:00:00")
            .build();
    private final EndpointHit endpointHit2 = EndpointHit.builder()
            .app("test APP")
            .uri("/test/uri/2")
            .ip("127.0.0.1")
            .timestamp("2020-05-05 11:00:00")
            .build();

    @Test
    public void shouldAddHitAndGetStats() {
        ResponseEntity<Object> response = statsClient.getStats(
                LocalDateTime.parse(endpointHit1.getTimestamp(), StatsCommonUtils.DT_FORMATTER),
                LocalDateTime.parse(endpointHit2.getTimestamp(), StatsCommonUtils.DT_FORMATTER)
        );
        try {
            List<ViewStats> stats = Arrays.asList(mapper.readValue(
                    mapper.writeValueAsString(response.getBody()), ViewStats[].class));

            assertNotNull(stats);
            assertTrue(stats.isEmpty());
        } catch (IOException exception) {
            throw new ClassCastException(exception.getMessage());
        }

        statsClient.addHit(endpointHit1.getApp(), endpointHit1.getUri(), endpointHit1.getIp(),
                LocalDateTime.parse(endpointHit1.getTimestamp(), StatsCommonUtils.DT_FORMATTER));
        statsClient.addHit(endpointHit2.getApp(), endpointHit2.getUri(), endpointHit2.getIp(),
                LocalDateTime.parse(endpointHit2.getTimestamp(), StatsCommonUtils.DT_FORMATTER));
        statsClient.addHit(endpointHit2.getApp(), endpointHit2.getUri(), endpointHit2.getIp(),
                LocalDateTime.parse(endpointHit2.getTimestamp(), StatsCommonUtils.DT_FORMATTER));

        response = statsClient.getStats(
                LocalDateTime.parse(endpointHit1.getTimestamp(), StatsCommonUtils.DT_FORMATTER),
                LocalDateTime.parse(endpointHit2.getTimestamp(), StatsCommonUtils.DT_FORMATTER)
        );
        try {
            List<ViewStats> stats = Arrays.asList(mapper.readValue(
                    mapper.writeValueAsString(response.getBody()), ViewStats[].class));

            assertNotNull(stats);
            assertEquals(2, stats.size());

            ViewStats viewStats1 = stats.get(0);
            ViewStats viewStats2 = stats.get(1);

            assertEquals(endpointHit2.getApp(), viewStats1.getApp());
            assertEquals(endpointHit2.getUri(), viewStats1.getUri());
            assertEquals(2, viewStats1.getHits());

            assertEquals(endpointHit1.getApp(), viewStats2.getApp());
            assertEquals(endpointHit1.getUri(), viewStats2.getUri());
            assertEquals(1, viewStats2.getHits());
        } catch (IOException exception) {
            throw new ClassCastException(exception.getMessage());
        }
    }
}

