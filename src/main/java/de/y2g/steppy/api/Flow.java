package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Sink;
import de.y2g.steppy.api.streaming.Source;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Flow<I, R> {
    default Result<R> invoke(Configurations configurations, I input) throws ExecutionException {
        return invoke(configurations, Collections.singletonList(input)).stream().findFirst().get();
    }

    default Result<R> invoke(Configurations configurations) throws ExecutionException {
        return invoke(configurations, (I)null);
    }

    default Result<R> invoke(I input) throws ExecutionException {
        return invoke(Configurations.empty(), input);
    }

    default Collection<Result<R>> invoke(List<I> input) throws ExecutionException {
        return invoke(Configurations.empty(), input);
    }

    default Result<R> invoke() throws ExecutionException {
        return invoke(Configurations.empty());
    }

    Collection<Result<R>> invoke(Configurations configurations, Collection<I> input) throws ExecutionException;

    default void stream(Source<I> source, Sink<R> sink) throws ExecutionException {
        stream(Configurations.empty(), source, sink);
    }

    void stream(Configurations configurations, Source<I> source, Sink<R> sink) throws ExecutionException;
}
