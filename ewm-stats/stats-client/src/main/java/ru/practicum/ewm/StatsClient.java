package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StatsClient {

    private final RestClient restClient;

    public StatsClient(@Value("${stats-service.url}") String serverUrl) {
        this.restClient = RestClient.create(serverUrl);
    }

    public void saveHit(EndpointHitDto endpointHitDto) {
        try {
            log.info("Stats-client: request to save hit {}", endpointHitDto);
            ResponseEntity<Void> response = restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Stats-client: Hit saved code {}", response.getStatusCode());
            } else {
                log.error("Stats-client: hit saved with error code {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Stats-client: ", e);
            throw new RuntimeException("Stats-client: failed to save hit", e);
        }
    }

    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique) {

        String uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", encodeDate(start))
                .queryParam("end", encodeDate(end))
                .queryParam("uris", String.join(",", uris))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .build()
                .toUriString();
        try {
            ResponseEntity<List<ViewStatsDto>> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Stats-client: request getStats was successful.");
                return response.getBody();
            } else {
                log.debug("Stats-client: request getStats failed with status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Stats-client: ", e);
            throw new RuntimeException("Stats-client: Failed to get stats", e);
        }
    }

    private String encodeDate(LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8).replace("+", " ").replace("%3A", ":");
    }

}
