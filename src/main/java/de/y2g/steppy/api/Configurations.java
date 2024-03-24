package de.y2g.steppy.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Configurations {
    private final Set<Object> configurations;

    public Configurations(Configurations other, List<Object> configurations) {
        this(Stream.concat(other.configurations.stream(), configurations.stream()).toList());
    }

    public Configurations(List<Object> configurations) {
        this.configurations = configurations.stream().collect(Collectors.toSet());
    }

    public static Configurations of(Object... configurations) {
        return new Configurations(List.of(configurations));
    }

    public static Configurations empty() {
        return new Configurations(List.of());
    }

    public <T> void put(T configuration) {
        configurations.add(configuration);
    }

    public <T> T get(Class<T> type) {
        return (T)configurations.stream().filter(Objects::nonNull).filter(o -> type.isAssignableFrom(o.getClass())).findFirst()
            .orElse(null);
    }

    public boolean contains(Class<?> configType) {
        return configurations.stream().anyMatch(o -> configType.isAssignableFrom(o.getClass()));
    }
}
