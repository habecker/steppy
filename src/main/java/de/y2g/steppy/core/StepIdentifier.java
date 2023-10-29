package de.y2g.steppy.core;

public record StepIdentifier(String name) {
    @Override
    public String toString() {
        return name;
    }
}
