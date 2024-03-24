package de.y2g.steppy.api;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Configurations {
    private final Map<Class<?>, Object> configurations;

    public Configurations(Configurations other, List<Object> configurations) {
        this(Stream.concat(other.configurations.values().stream(), configurations.stream()).toList());
    }

    public Configurations(List<Object> configurations) {
        this.configurations = configurations.stream().collect(Collectors.toMap(Object::getClass, Function.identity()));
    }

    public static Configurations of(Object... configurations) {
        return new Configurations(List.of(configurations));
    }

    public static Configurations empty() {
        return new Configurations(List.of());
    }

    public <T> void put(T configuration) {
        configurations.put(configuration.getClass(), configuration);
    }

    public <T> T get(Class<T> type) {
        return type.cast(configurations.get(type));
    }

    public boolean contains(Class<?> configType) {
        return configurations.containsKey(configType);
    }
}
