package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

import java.util.Collection;
import java.util.Collections;

public interface Flow<C, I, R> {
    default Result<R> invoke(C configuration, I input) throws ExecutionException {
        return invoke(configuration, Collections.singletonList(input)).stream().findFirst().get();
    }

    default Result<R> invoke(C configuration) throws ExecutionException {
        return invoke(configuration, (I) null);
    }

    Collection<Result<R>> invoke(C configuration, Collection<I> input) throws ExecutionException;
}
