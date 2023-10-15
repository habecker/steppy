package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.Result;

import java.util.function.Consumer;

public interface Sink<T> extends Consumer<Result<T>> {
    void close();
}
