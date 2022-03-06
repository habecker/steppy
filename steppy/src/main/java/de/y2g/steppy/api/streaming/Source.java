package de.y2g.steppy.api.streaming;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Source<I> {
    boolean isActive();
    boolean next(Duration timeout, Consumer<I> consumer) throws InterruptedException;
    void close();
}
