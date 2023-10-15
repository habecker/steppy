package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public class AbortStep implements Step<None, String, String> {
    @Override
    public String invoke(Context<None> context, String input) throws ExecutionException {
        context.abort();
        return null;
    }
}
