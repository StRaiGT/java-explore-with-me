package ru.practicum.stats_common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
public class EndpointHit {
    @NotBlank
    String app;

    @NotBlank
    String uri;

    @NotBlank
    String ip;

    @NotBlank
    String timestamp;

    @Override
    public String toString() {
        return "EndpointHit{" +
                "app='" + app + '\'' +
                ", uri='" + uri + '\'' +
                ", ip='" + ip + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
