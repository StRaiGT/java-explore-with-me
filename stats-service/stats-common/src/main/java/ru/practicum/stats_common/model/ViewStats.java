package ru.practicum.stats_common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ViewStats {
    String app;
    String uri;
    Long hits;

    @Override
    public String toString() {
        return "ViewStats{" +
                "app='" + app + '\'' +
                ", uri='" + uri + '\'' +
                ", hits=" + hits +
                '}';
    }
}
