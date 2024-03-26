package de.y2g.steppy.core;

public record Dependency(String name, Class<?> type) {
    @Override
    public String toString() {
        return name + " (" + type.getSimpleName() + ")";
    }
}
