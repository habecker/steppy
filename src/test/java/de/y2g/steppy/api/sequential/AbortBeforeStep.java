package de.y2g.steppy.api.sequential;

import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.Step;
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
