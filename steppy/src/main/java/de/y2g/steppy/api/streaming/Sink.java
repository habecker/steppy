package de.y2g.steppy.api.streaming;

import java.util.function.Consumer;

public interface Sink<T> extends Consumer<T> {
    void close();
}
