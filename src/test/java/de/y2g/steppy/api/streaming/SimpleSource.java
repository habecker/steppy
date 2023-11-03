package de.y2g.steppy.api.streaming;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

public class SimpleSource<T> implements Source<T> {
    private final List<Throwable> failures = new ArrayList<>();

    private Iterator<T> source;

    public SimpleSource(Stream<T> source) {
        this.source = source.iterator();
    }

    public List<Throwable> getFailures() {
        return failures;
    }

    @Override
    public boolean isActive() {
        return source.hasNext();
    }

    @Override
    public boolean next(Duration timeout, Consumer<T> consumer) throws InterruptedException {
        if (source.hasNext()) {
            consumer.accept(source.next());
            return source.hasNext();
        }
        return false;
    }

    @Override
    public void onFailure(T input, Throwable throwable) {
        failures.add(throwable);
    }

    @Override
    public void close() {
        source = emptyIterator();
    }
}
