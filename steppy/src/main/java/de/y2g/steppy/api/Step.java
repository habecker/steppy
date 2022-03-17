package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public interface Step<C, I, R> {
    R invoke(Context<C> context, I input) throws ExecutionException;
}
