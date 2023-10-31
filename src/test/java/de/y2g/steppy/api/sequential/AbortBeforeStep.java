package de.y2g.steppy.api.sequential;

import de.y2g.steppy.api.*;
import de.y2g.steppy.api.exception.ExecutionException;

public class AbortBeforeStep implements Step<None, String, String> {

    @Before(Scope.STEP)
    public void abort(Context<None> context) {
        context.abort();

    }
    @Override
    public String invoke(Context<None> context, String input) throws ExecutionException {
        context.abort();
        return null;
    }
}
