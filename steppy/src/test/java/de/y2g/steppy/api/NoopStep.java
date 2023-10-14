package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public class NoopStep implements Step<Object, None, None> {
    @Override
    public None invoke(Context<Object> context, None input) throws ExecutionException {
        return input;
    }
}
