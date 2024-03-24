package de.y2g.steppy.api.streaming;

import java.time.Duration;
import java.util.function.Consumer;

public class InfiniteSource<T> implements Source<T> {
    private final T source;

    private boolean closed;

    public InfiniteSource(T source) {
        this.source = source;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean next(Duration timeout, Consumer<T> consumer) throws InterruptedException {
        consumer.accept(source);
        Thread.sleep(timeout.toMillis());
        return true;
    }

    @Override
    public void onFailure(T input, Throwable throwable) {
        // noop
    }

    @Override
    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
