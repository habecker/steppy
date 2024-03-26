package de.y2g.steppy.api;

public final class None {
    public static None value() {
        return new None();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof None;
    }
}